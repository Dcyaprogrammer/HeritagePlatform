package com.heritage.platform.discovery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.discovery.dto.NamedRow;
import com.heritage.platform.discovery.dto.PublicResourceDetail;
import com.heritage.platform.discovery.dto.PublicResourceSummary;
import com.heritage.platform.discovery.dto.SlicePage;
import com.heritage.platform.discovery.taxonomy.HeritageTypeGroup;
import com.heritage.platform.discovery.taxonomy.TaxonomyCatalog;
import com.heritage.platform.discovery.taxonomy.TaxonomyOption;
import com.heritage.platform.common.ApiResponse;

/**
 * 访客大厅用的公开接口，不走登录；数据侧只依赖 {@link PublicDiscoveryService} 里写死的 APPROVED 条件。
 */
@RestController
@RequestMapping("/api/public")
public class PublicDiscoveryController {

	private final PublicDiscoveryService discoveryService;
	private final TaxonomyCatalog taxonomy;

	public PublicDiscoveryController(PublicDiscoveryService discoveryService, TaxonomyCatalog taxonomy) {
		this.discoveryService = discoveryService;
		this.taxonomy = taxonomy;
	}

	@GetMapping("/categories")
	public ApiResponse<List<NamedRow>> categories() {
		return ApiResponse.success(discoveryService.listCategories());
	}

	@GetMapping("/tags")
	public ApiResponse<List<NamedRow>> tags() {
		return ApiResponse.success(discoveryService.listTags());
	}

	@GetMapping("/dynasties")
	public ApiResponse<List<TaxonomyOption>> dynasties() {
		return ApiResponse.success(taxonomy.getDynasties());
	}

	@GetMapping("/provinces")
	public ApiResponse<List<TaxonomyOption>> provinces() {
		return ApiResponse.success(taxonomy.getProvinces());
	}

	@GetMapping("/heritage-type-groups")
	public ApiResponse<List<HeritageTypeGroup>> heritageTypeGroups() {
		return ApiResponse.success(taxonomy.getHeritageTypeGroups());
	}

	/**
	 * @param q                 标题关键词，可空
	 * @param categoryId        原分类表 id，可空
	 * @param tags              逗号分隔的标签 id，多个表示同时满足（AND）
	 * @param dynastyCode       朝代代码，可空；支持逗号分隔多选（如 TANG,SOUTHERN_SONG）
	 * @param eraFrom           年代筛选起始（与 eraTo 同填或同空）
	 * @param eraTo             年代筛选截止
	 * @param provinceCode      省级行政区代码，可空；支持逗号分隔多选（如 BJ,SH）
	 * @param heritageTypeCode  文物类型：叶子代码或大类代码（如 HTG_RITE），支持逗号分隔多选，可空
	 * @param page              从 0 开始
	 * @param size              每页条数，默认 10
	 */
	@GetMapping("/resources")
	public ApiResponse<?> resources(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String tags,
			@RequestParam(required = false) String dynastyCode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eraFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eraTo,
			@RequestParam(required = false) String provinceCode,
			@RequestParam(required = false) String heritageTypeCode,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if ((eraFrom != null) != (eraTo != null)) {
			return ApiResponse.error(400, "年代起止需同时填写或同时留空");
		}
		if (eraFrom != null && eraTo != null && eraFrom.isAfter(eraTo)) {
			return ApiResponse.error(400, "起始时间不能晚于截止时间");
		}

		List<String> dNorms = null;
		if (dynastyCode != null && !dynastyCode.isBlank()) {
			List<String> parsed = new ArrayList<>();
			for (String p : dynastyCode.split(",")) {
				String t = p == null ? "" : p.trim();
				if (t.isEmpty()) {
					continue;
				}
				String d = taxonomy.normalizeDynastyCode(t).orElse(null);
				if (d == null) {
					return ApiResponse.error(400, "无效的朝代代码");
				}
				if (!parsed.contains(d)) {
					parsed.add(d);
				}
			}
			if (!parsed.isEmpty()) {
				dNorms = parsed;
			}
		}

		List<String> pNorms = null;
		if (provinceCode != null && !provinceCode.isBlank()) {
			List<String> parsed = new ArrayList<>();
			for (String p : provinceCode.split(",")) {
				String t = p == null ? "" : p.trim();
				if (t.isEmpty()) {
					continue;
				}
				String norm = taxonomy.normalizeProvinceCode(t).orElse(null);
				if (norm == null) {
					return ApiResponse.error(400, "无效的地区代码");
				}
				if (!parsed.contains(norm)) {
					parsed.add(norm);
				}
			}
			if (!parsed.isEmpty()) {
				pNorms = parsed;
			}
		}

		List<String> hCodes = null;
		if (heritageTypeCode != null && !heritageTypeCode.isBlank()) {
			List<String> parsed = new ArrayList<>();
			for (String p : heritageTypeCode.split(",")) {
				String t = p == null ? "" : p.trim();
				if (t.isEmpty()) {
					continue;
				}
				Optional<List<String>> resolved = taxonomy.resolveHeritageTypeCodesForFilter(t);
				if (resolved.isEmpty()) {
					return ApiResponse.error(400, "无效的文物类型代码");
				}
				for (String c : resolved.get()) {
					if (!parsed.contains(c)) {
						parsed.add(c);
					}
				}
			}
			if (!parsed.isEmpty()) {
				hCodes = parsed;
			}
		}

		List<Long> tagIds = PublicDiscoveryService.parseTagIds(tags);
		SlicePage<PublicResourceSummary> slice = discoveryService.search(q, categoryId, tagIds, dNorms, eraFrom, eraTo,
				pNorms, hCodes, page, size);
		return ApiResponse.success(slice);
	}

	@GetMapping("/resources/{id}")
	public ApiResponse<PublicResourceDetail> resourceDetail(@PathVariable Long id) {
		return ApiResponse.success(discoveryService.findApprovedDetail(id).orElse(null));
	}
}
