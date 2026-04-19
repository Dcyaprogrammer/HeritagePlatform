package com.heritage.platform.discovery.taxonomy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 年代（朝代）、地区（省级）、文物类型（三大类及叶子）的静态目录与名称解析。
 */
@Component
public class TaxonomyCatalog {

	private final List<TaxonomyOption> dynasties;
	private final List<TaxonomyOption> provinces;
	private final List<HeritageTypeGroup> heritageTypeGroups;
	private final Map<String, String> dynastyNameByCode;
	private final Map<String, String> provinceNameByCode;
	private final Map<String, String> heritageTypeLabelByCode;
	/** 大类代码 → 组内全部叶子类型代码（不可变列表） */
	private final Map<String, List<String>> heritageLeafCodesByGroupCode;

	/** 用于由录入年份推断朝代（自新至旧，先匹配者优先） */
	private final List<DynastyYearRange> dynastyRangesNewestFirst;

	public TaxonomyCatalog() {
		this.dynasties = buildDynasties();
		this.provinces = buildProvinces();
		this.heritageTypeGroups = buildHeritageTypes();
		this.dynastyNameByCode = indexByCode(dynasties);
		this.provinceNameByCode = indexByCode(provinces);
		this.heritageTypeLabelByCode = indexHeritageLabels(heritageTypeGroups);
		this.heritageLeafCodesByGroupCode = indexHeritageGroupToLeafCodes(heritageTypeGroups);
		this.dynastyRangesNewestFirst = buildDynastyRangesForInference();
	}

	/**
	 * 根据资源在库中的录入时间（created_at 的年份）推断所属朝代代码；无区间命中时返回 UNKNOWN。
	 */
	public String inferDynastyCodeFromGregorianYear(int year) {
		for (DynastyYearRange r : dynastyRangesNewestFirst) {
			if (year >= r.startYear() && year <= r.endYear()) {
				return r.code();
			}
		}
		return "UNKNOWN";
	}

	/**
	 * 筛选「朝代」时：用 {@code YEAR(created_at)} 与该朝代大致公历区间比较。
	 * 返回 empty 表示不在 SQL 中按朝代过滤（UNKNOWN）；若该朝代无法用语义化年份表达则返回仅含负数的区间由调用方处理。
	 */
	public Optional<int[]> sqlInclusiveYearRangeForDynastyFilter(String normalizedCode) {
		if (normalizedCode == null || normalizedCode.isBlank() || "UNKNOWN".equals(normalizedCode)) {
			return Optional.empty();
		}
		return switch (normalizedCode) {
			case "PRC" -> Optional.of(new int[] { 1949, 9999 });
			case "REPUBLIC_OF_CHINA" -> Optional.of(new int[] { 1912, 1948 });
			case "QING" -> Optional.of(new int[] { 1644, 1911 });
			case "MING" -> Optional.of(new int[] { 1368, 1644 });
			case "YUAN" -> Optional.of(new int[] { 1271, 1368 });
			case "JIN" -> Optional.of(new int[] { 1115, 1234 });
			case "WESTERN_XIA" -> Optional.of(new int[] { 1038, 1227 });
			case "SOUTHERN_SONG" -> Optional.of(new int[] { 1127, 1279 });
			case "NORTHERN_SONG" -> Optional.of(new int[] { 960, 1127 });
			case "LIAO" -> Optional.of(new int[] { 916, 1125 });
			case "FIVE_DYNASTIES" -> Optional.of(new int[] { 907, 960 });
			case "TANG" -> Optional.of(new int[] { 618, 907 });
			case "SUI" -> Optional.of(new int[] { 581, 618 });
			case "NORTHERN_SOUTHERN" -> Optional.of(new int[] { 420, 589 });
			case "EASTERN_JIN" -> Optional.of(new int[] { 317, 420 });
			case "WESTERN_JIN" -> Optional.of(new int[] { 266, 316 });
			case "THREE_KINGDOMS" -> Optional.of(new int[] { 220, 280 });
			case "EASTERN_HAN" -> Optional.of(new int[] { 25, 220 });
			case "XIN" -> Optional.of(new int[] { 9, 23 });
			case "WESTERN_HAN" -> Optional.of(new int[] { 1, 8 });
			case "SIXTEEN_KINGDOMS" -> Optional.of(new int[] { 304, 439 });
			case "SPRING_AUTUMN" -> Optional.of(new int[] { -770, -476 });
			case "EASTERN_ZHOU" -> Optional.of(new int[] { -770, -256 });
			case "WARRING_STATES" -> Optional.of(new int[] { -475, -221 });
			case "QIN" -> Optional.of(new int[] { -221, -207 });
			case "SHANG" -> Optional.of(new int[] { -1600, -1046 });
			case "XIA" -> Optional.of(new int[] { -2070, -1600 });
			case "WESTERN_ZHOU" -> Optional.of(new int[] { -1046, -771 });
			default -> Optional.empty();
		};
	}

	/**
	 * 从地点描述中匹配省级行政区（按名称长度优先，避免「河南」误伤「河北」）。
	 */
	public Optional<String> inferProvinceCodeFromLocation(String locationName) {
		if (locationName == null || locationName.isBlank()) {
			return Optional.empty();
		}
		String s = locationName.trim();
		List<TaxonomyOption> sorted = new ArrayList<>(provinces);
		sorted.sort(Comparator.comparingInt((TaxonomyOption o) -> o.getName().length()).reversed());
		for (TaxonomyOption p : sorted) {
			String full = p.getName();
			if (s.contains(full)) {
				return Optional.of(p.getCode());
			}
			String shortName = full.replace("特别行政区", "").replace("维吾尔自治区", "").replace("回族自治区", "")
					.replace("壮族自治区", "").replace("自治区", "").replace("省", "").replace("市", "");
			if (!shortName.isBlank() && s.contains(shortName)) {
				return Optional.of(p.getCode());
			}
		}
		return Optional.empty();
	}

	/** 用于 SQL：{@code location_name LIKE :pat} */
	public String provinceLocationLikePattern(String normalizedProvinceCode) {
		return provinceName(normalizedProvinceCode).map(n -> {
			String core = n.replace("特别行政区", "").replace("维吾尔自治区", "").replace("回族自治区", "")
					.replace("壮族自治区", "").replace("自治区", "").replace("省", "").replace("市", "");
			if (core.isBlank()) {
				core = n;
			}
			return "%" + core + "%";
		}).orElse("%");
	}

	private static List<DynastyYearRange> buildDynastyRangesForInference() {
		List<DynastyYearRange> list = new ArrayList<>();
		// 新朝优先匹配（与 sql 区间一致，略宽处可接受）
		list.add(new DynastyYearRange("PRC", 1949, 9999));
		list.add(new DynastyYearRange("REPUBLIC_OF_CHINA", 1912, 1948));
		list.add(new DynastyYearRange("QING", 1644, 1911));
		list.add(new DynastyYearRange("MING", 1368, 1644));
		list.add(new DynastyYearRange("YUAN", 1271, 1368));
		list.add(new DynastyYearRange("JIN", 1115, 1234));
		list.add(new DynastyYearRange("WESTERN_XIA", 1038, 1227));
		list.add(new DynastyYearRange("SOUTHERN_SONG", 1127, 1279));
		list.add(new DynastyYearRange("NORTHERN_SONG", 960, 1127));
		list.add(new DynastyYearRange("LIAO", 916, 1125));
		list.add(new DynastyYearRange("FIVE_DYNASTIES", 907, 960));
		list.add(new DynastyYearRange("TANG", 618, 907));
		list.add(new DynastyYearRange("SUI", 581, 618));
		list.add(new DynastyYearRange("NORTHERN_SOUTHERN", 420, 589));
		list.add(new DynastyYearRange("EASTERN_JIN", 317, 420));
		list.add(new DynastyYearRange("WESTERN_JIN", 266, 316));
		list.add(new DynastyYearRange("THREE_KINGDOMS", 220, 280));
		list.add(new DynastyYearRange("EASTERN_HAN", 25, 220));
		list.add(new DynastyYearRange("XIN", 9, 23));
		list.add(new DynastyYearRange("WESTERN_HAN", 1, 8));
		list.add(new DynastyYearRange("SIXTEEN_KINGDOMS", 304, 439));
		list.add(new DynastyYearRange("SPRING_AUTUMN", -770, -476));
		list.add(new DynastyYearRange("WARRING_STATES", -475, -221));
		list.add(new DynastyYearRange("EASTERN_ZHOU", -770, -256));
		list.add(new DynastyYearRange("QIN", -221, -207));
		list.add(new DynastyYearRange("SHANG", -1600, -1046));
		list.add(new DynastyYearRange("XIA", -2070, -1600));
		list.add(new DynastyYearRange("WESTERN_ZHOU", -1046, -771));
		return Collections.unmodifiableList(list);
	}

	private record DynastyYearRange(String code, int startYear, int endYear) {
	}

	public List<TaxonomyOption> getDynasties() {
		return dynasties;
	}

	public List<TaxonomyOption> getProvinces() {
		return provinces;
	}

	public List<HeritageTypeGroup> getHeritageTypeGroups() {
		return heritageTypeGroups;
	}

	public Optional<String> dynastyName(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(dynastyNameByCode.get(code.trim()));
	}

	public Optional<String> provinceName(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(provinceNameByCode.get(code.trim()));
	}

	public Optional<String> heritageTypeLabel(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(heritageTypeLabelByCode.get(code.trim()));
	}

	private static Map<String, String> indexByCode(List<TaxonomyOption> opts) {
		Map<String, String> m = new HashMap<>();
		for (TaxonomyOption o : opts) {
			m.put(o.getCode(), o.getName());
		}
		return Collections.unmodifiableMap(m);
	}

	private static Map<String, String> indexHeritageLabels(List<HeritageTypeGroup> groups) {
		Map<String, String> m = new HashMap<>();
		for (HeritageTypeGroup g : groups) {
			for (TaxonomyOption t : g.getTypes()) {
				String label = g.getGroupName() + " · " + t.getName();
				m.put(t.getCode(), label);
			}
		}
		return Collections.unmodifiableMap(m);
	}

	private static Map<String, List<String>> indexHeritageGroupToLeafCodes(List<HeritageTypeGroup> groups) {
		Map<String, List<String>> m = new HashMap<>();
		for (HeritageTypeGroup g : groups) {
			List<String> leaves = g.getTypes().stream().map(TaxonomyOption::getCode).collect(Collectors.toUnmodifiableList());
			m.put(g.getGroupCode(), leaves);
		}
		return Collections.unmodifiableMap(m);
	}

	private static List<TaxonomyOption> buildDynasties() {
		List<TaxonomyOption> list = new ArrayList<>();
		long id = 1;
		String[][] rows = {
				{ "XIA", "Xia" },
				{ "SHANG", "Shang" },
				{ "WESTERN_ZHOU", "Western Zhou" },
				{ "EASTERN_ZHOU", "Eastern Zhou" },
				{ "SPRING_AUTUMN", "Spring and Autumn" },
				{ "WARRING_STATES", "Warring States" },
				{ "QIN", "Qin" },
				{ "WESTERN_HAN", "Western Han" },
				{ "XIN", "Xin" },
				{ "EASTERN_HAN", "Eastern Han" },
				{ "THREE_KINGDOMS", "Three Kingdoms" },
				{ "WESTERN_JIN", "Western Jin" },
				{ "EASTERN_JIN", "Eastern Jin" },
				{ "SIXTEEN_KINGDOMS", "Sixteen Kingdoms" },
				{ "NORTHERN_SOUTHERN", "Northern and Southern Dynasties" },
				{ "SUI", "Sui" },
				{ "TANG", "Tang" },
				{ "FIVE_DYNASTIES", "Five Dynasties" },
				{ "LIAO", "Liao" },
				{ "NORTHERN_SONG", "Northern Song" },
				{ "WESTERN_XIA", "Western Xia" },
				{ "SOUTHERN_SONG", "Southern Song" },
				{ "JIN", "Jin" },
				{ "YUAN", "Yuan" },
				{ "MING", "Ming" },
				{ "QING", "Qing" },
				{ "REPUBLIC_OF_CHINA", "Republic of China" },
				{ "PRC", "People's Republic of China" },
				{ "UNKNOWN", "Unknown / Others" },
		};
		for (String[] row : rows) {
			list.add(new TaxonomyOption(id++, row[0], row[1]));
		}
		return Collections.unmodifiableList(list);
	}

	private static List<TaxonomyOption> buildProvinces() {
		List<TaxonomyOption> list = new ArrayList<>();
		long id = 1;
		String[][] rows = {
				{ "BJ", "Beijing" },
				{ "TJ", "Tianjin" },
				{ "SH", "Shanghai" },
				{ "CQ", "Chongqing" },
				{ "HE", "Hebei" },
				{ "SX", "Shanxi" },
				{ "LN", "Liaoning" },
				{ "JL", "Jilin" },
				{ "HL", "Heilongjiang" },
				{ "JS", "Jiangsu" },
				{ "ZJ", "Zhejiang" },
				{ "AH", "Anhui" },
				{ "FJ", "Fujian" },
				{ "JX", "Jiangxi" },
				{ "SD", "Shandong" },
				{ "HA", "Henan" },
				{ "HB", "Hubei" },
				{ "HN", "Hunan" },
				{ "GD", "Guangdong" },
				{ "HI", "Hainan" },
				{ "SC", "Sichuan" },
				{ "GZ", "Guizhou" },
				{ "YN", "Yunnan" },
				{ "SN", "Shaanxi" },
				{ "GS", "Gansu" },
				{ "QH", "Qinghai" },
				{ "TW", "Taiwan" },
				{ "NM", "Inner Mongolia" },
				{ "GX", "Guangxi" },
				{ "XZ", "Tibet" },
				{ "NX", "Ningxia" },
				{ "XJ", "Xinjiang" },
				{ "HK", "Hong Kong" },
				{ "MO", "Macao" },
		};
		for (String[] row : rows) {
			list.add(new TaxonomyOption(id++, row[0], row[1]));
		}
		return Collections.unmodifiableList(list);
	}

	private static List<HeritageTypeGroup> buildHeritageTypes() {
		long id = 1000;
		List<HeritageTypeGroup> groups = new ArrayList<>();
		groups.add(new HeritageTypeGroup("HTG_RITE", "Ritual and Furnishing", List.of(
				opt(id++, "RIT_BRONZE", "Bronze"),
				opt(id++, "RIT_JADE", "Jade"),
				opt(id++, "RIT_CERAMIC", "Ceramics"),
				opt(id++, "RIT_SCULPTURE", "Sculpture"),
				opt(id++, "RIT_BUDDHIST", "Buddhist Relic"),
				opt(id++, "RIT_ARCHITECTURE", "Ancient Architecture"))));
		groups.add(new HeritageTypeGroup("HTG_LIFE", "Life and Technology", List.of(
				opt(id++, "LIFE_GOLD_SILVER", "Gold and Silverware"),
				opt(id++, "LIFE_LACQUER_WOOD", "Lacquerware and Woodware"),
				opt(id++, "LIFE_STONE", "Stoneware"),
				opt(id++, "LIFE_TEXTILE", "Textiles and Costumes"),
				opt(id++, "LIFE_METROLOGY", "Metrology Instruments"),
				opt(id++, "LIFE_COIN", "Coins"),
				opt(id++, "LIFE_GLASS", "Glassware"),
				opt(id++, "LIFE_TRANSPORT", "Transportation Tools"))));
		groups.add(new HeritageTypeGroup("HTG_CULTURE", "Culture and Art", List.of(
				opt(id++, "CULT_PAINTING_CALLIGRAPHY", "Painting and Calligraphy"),
				opt(id++, "CULT_INSCRIPTIONS_BAMBOO", "Inscriptions and Bamboo Slips"),
				opt(id++, "CULT_SEALS_TOKENS", "Seals and Tokens"),
				opt(id++, "CULT_STATIONERY", "Study Utensils"),
				opt(id++, "CULT_MUSIC", "Musical Relic"),
				opt(id++, "CULT_BOOKS_DOCUMENTS", "Books and Documents"))));
		groups.add(new HeritageTypeGroup("HTG_OTHER", "Others", List.of(
				opt(id++, "HERITAGE_OTHER", "Others"))));
		return Collections.unmodifiableList(groups);
	}

	private static TaxonomyOption opt(long id, String code, String name) {
		return new TaxonomyOption(id, code, name);
	}

	/** 校验 code 是否为合法朝代代码（大小写不敏感则归一化） */
	public Optional<String> normalizeDynastyCode(String raw) {
		return normalizeAgainst(dynastyNameByCode.keySet(), raw);
	}

	public Optional<String> normalizeProvinceCode(String raw) {
		return normalizeAgainst(provinceNameByCode.keySet(), raw);
	}

	public Optional<String> normalizeHeritageTypeCode(String raw) {
		return normalizeAgainst(heritageTypeLabelByCode.keySet(), raw);
	}

	/**
	 * 将检索参数解析为叶子类型代码列表：传入大类代码时返回该组全部叶子；传入叶子代码时返回单元素列表。
	 */
	public Optional<List<String>> resolveHeritageTypeCodesForFilter(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String t = raw.trim();
		if (heritageLeafCodesByGroupCode.containsKey(t)) {
			return Optional.of(heritageLeafCodesByGroupCode.get(t));
		}
		String upper = t.toUpperCase(Locale.ROOT);
		if (heritageLeafCodesByGroupCode.containsKey(upper)) {
			return Optional.of(heritageLeafCodesByGroupCode.get(upper));
		}
		return normalizeHeritageTypeCode(t).map(List::of);
	}

	private static Optional<String> normalizeAgainst(java.util.Set<String> codes, String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String t = raw.trim();
		if (codes.contains(t)) {
			return Optional.of(t);
		}
		String upper = t.toUpperCase(Locale.ROOT);
		if (codes.contains(upper)) {
			return Optional.of(upper);
		}
		return Optional.empty();
	}
}
