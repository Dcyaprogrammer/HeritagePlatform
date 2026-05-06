package com.heritage.platform.discovery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.heritage.platform.discovery.dto.NamedRow;
import com.heritage.platform.discovery.dto.PublicResourceDetail;
import com.heritage.platform.discovery.dto.PublicResourceSummary;
import com.heritage.platform.discovery.dto.SlicePage;
import com.heritage.platform.discovery.taxonomy.TaxonomyCatalog;

/*
 * 面向访客的大厅检索：只读、只暴露已通过审核的资源。
 * <p>
 * 年代/地区/类型筛选全部下沉到 SQL：朝代与年代区间基于 {@code created_at}；地区基于 {@code location_name}
 * 文本匹配；类型基于 {@code resources.heritage_type_code}。
 */
@Service
public class PublicDiscoveryService {

	private static final String BASE_FROM = """
			FROM resources r
			INNER JOIN categories c ON c.id = r.category_id
			WHERE r.status = 'APPROVED'
			""";

	private final NamedParameterJdbcTemplate jdbc;
	private final TaxonomyCatalog taxonomy;

	public PublicDiscoveryService(NamedParameterJdbcTemplate jdbc, TaxonomyCatalog taxonomy) {
		this.jdbc = jdbc;
		this.taxonomy = taxonomy;
	}
	

	public List<NamedRow> listCategories() {
		String sql = "SELECT id, name FROM categories ORDER BY name ASC";
		return jdbc.query(sql, Map.of(), (rs, row) -> new NamedRow(rs.getLong("id"), rs.getString("name")));
	}

	public List<NamedRow> listTags() {
		String sql = "SELECT id, name FROM tags ORDER BY name ASC";
		return jdbc.query(sql, Map.of(), (rs, row) -> new NamedRow(rs.getLong("id"), rs.getString("name")));
	}

	public SlicePage<PublicResourceSummary> search(String keyword, Integer categoryId, List<Long> tagIds,
			List<String> dynastyCodes, LocalDate eraFrom, LocalDate eraTo, List<String> provinceCodes,
			List<String> heritageTypeCodes, int page, int size) {
		if (page < 0) {
			page = 0;
		}
		if (size < 1) {
			size = 10;
		}
		if (size > 100) {
			size = 100;
		}

		List<Long> tagFilter = tagIds == null ? List.of()
				: tagIds.stream().distinct().collect(Collectors.toList());

		MapSqlParameterSource params = new MapSqlParameterSource();
		StringBuilder extra = new StringBuilder();

		if (keyword != null && !keyword.isBlank()) {
			extra.append(" AND (r.title LIKE :kw OR COALESCE(r.description, '') LIKE :kw OR COALESCE(r.location_name, '') LIKE :kw) ");
			params.addValue("kw", "%" + keyword.trim() + "%");
		}
		if (categoryId != null) {
			extra.append(" AND r.category_id = :categoryId ");
			params.addValue("categoryId", categoryId);
		}
		if (!tagFilter.isEmpty()) {
			extra.append("""
					 AND (
					   SELECT COUNT(DISTINCT rt.tag_id)
					   FROM resource_tags rt
					   WHERE rt.resource_id = r.id AND rt.tag_id IN (:tagIds)
					 ) = :tagCount
					""");
			params.addValue("tagIds", tagFilter);
			params.addValue("tagCount", tagFilter.size());
		}

		if (dynastyCodes != null && !dynastyCodes.isEmpty()) {
			List<String> dynastyOr = new ArrayList<>();
			for (int i = 0; i < dynastyCodes.size(); i++) {
				String dynastyCode = dynastyCodes.get(i);
				Optional<int[]> yr = taxonomy.sqlInclusiveYearRangeForDynastyFilter(dynastyCode);
				if (yr.isEmpty()) {
					// UNKNOWN：该项不参与过滤
					continue;
				}
				int y1 = yr.get()[0];
				int y2 = yr.get()[1];
				if (y1 < 1 || y2 < 1) {
					extra.append(" AND 1=0 ");
					dynastyOr.clear();
					break;
				}
				String y1Key = "dY1_" + i;
				String y2Key = "dY2_" + i;
				dynastyOr.add("YEAR(r.created_at) BETWEEN :" + y1Key + " AND :" + y2Key);
				params.addValue(y1Key, Math.min(y1, y2));
				params.addValue(y2Key, Math.max(y1, y2));
			}
			if (!dynastyOr.isEmpty()) {
				extra.append(" AND (").append(String.join(" OR ", dynastyOr)).append(") ");
			}
		}

		if (provinceCodes != null && !provinceCodes.isEmpty()) {
			List<String> provinceOr = new ArrayList<>();
			for (int i = 0; i < provinceCodes.size(); i++) {
				String key = "locPat_" + i;
				provinceOr.add("r.location_name LIKE :" + key);
				params.addValue(key, taxonomy.provinceLocationLikePattern(provinceCodes.get(i)));
			}
			extra.append(" AND (").append(String.join(" OR ", provinceOr)).append(") ");
		}

		if (eraFrom != null && eraTo != null) {
			extra.append(" AND DATE(r.created_at) BETWEEN :eraFrom AND :eraTo ");
			params.addValue("eraFrom", java.sql.Date.valueOf(eraFrom));
			params.addValue("eraTo", java.sql.Date.valueOf(eraTo));
		}

		if (heritageTypeCodes != null && !heritageTypeCodes.isEmpty()) {
			extra.append(" AND r.heritage_type_code IN (:heritageTypeCodes) ");
			params.addValue("heritageTypeCodes", heritageTypeCodes);
		}

		String countSql = "SELECT COUNT(*) " + BASE_FROM + extra;
		Long total = jdbc.queryForObject(countSql, params, Long.class);
		if (total == null) {
			total = 0L;
		}

		params.addValue("limit", size);
		params.addValue("offset", page * size);

		String listSql = """
				SELECT r.id, r.title, r.description, r.location_name, r.category_id, c.name AS category_name,
				       r.heritage_type_code,
				       r.created_at, r.updated_at,
				       COALESCE(
				         (SELECT a.file_path FROM attachments a WHERE a.resource_id = r.id AND a.file_type = 'image' ORDER BY a.created_at ASC LIMIT 1),
				         (SELECT a.thumbnail_path FROM attachments a WHERE a.resource_id = r.id AND a.file_type = 'video' AND a.thumbnail_path IS NOT NULL ORDER BY a.created_at ASC LIMIT 1)
				       ) AS cover_url,
				       EXISTS(SELECT 1 FROM attachments a WHERE a.resource_id = r.id AND a.file_type = 'video') AS has_video,
				       COALESCE((SELECT COUNT(*) FROM comments cm WHERE cm.resource_id = r.id), 0) AS comment_count,
				       COALESCE((SELECT COUNT(*) FROM likes l WHERE l.resource_id = r.id), 0) AS like_count,
				       COALESCE((SELECT COUNT(*) FROM favorites f WHERE f.resource_id = r.id), 0) AS favorite_count
				""" + BASE_FROM + extra + """
				ORDER BY r.updated_at DESC
				LIMIT :limit OFFSET :offset
				""";

		List<PublicResourceSummary> rows = jdbc.query(listSql, params, new SummaryMapper());
		enrichSummaryTags(rows);

		SlicePage<PublicResourceSummary> out = new SlicePage<>();
		out.setItems(rows);
		out.setTotal(total);
		out.setPage(page);
		out.setSize(size);
		return out;
	}

	private void enrichSummaryTags(List<PublicResourceSummary> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}

		List<Long> resourceIds = rows.stream()
				.map(PublicResourceSummary::getId)
				.filter(id -> id != null)
				.distinct()
				.toList();
		if (resourceIds.isEmpty()) {
			return;
		}

		String tagSql = """
				SELECT rt.resource_id, t.id, t.name
				FROM resource_tags rt
				INNER JOIN tags t ON t.id = rt.tag_id
				WHERE rt.resource_id IN (:resourceIds)
				ORDER BY rt.resource_id ASC, t.name ASC
				""";
		MapSqlParameterSource tagParams = new MapSqlParameterSource("resourceIds", resourceIds);
		List<Map<String, Object>> tagRows = jdbc.queryForList(tagSql, tagParams);
		Map<Long, List<NamedRow>> tagMap = new java.util.HashMap<>();
		for (Map<String, Object> row : tagRows) {
			Long resourceId = ((Number) row.get("resource_id")).longValue();
			Long tagId = ((Number) row.get("id")).longValue();
			String tagName = (String) row.get("name");
			tagMap.computeIfAbsent(resourceId, k -> new ArrayList<>())
					.add(new NamedRow(tagId, tagName));
		}

		for (PublicResourceSummary summary : rows) {
			List<NamedRow> tags = tagMap.get(summary.getId());
			summary.setTags(tags != null ? tags : List.of());
		}
	}

	public Optional<PublicResourceDetail> findApprovedDetail(Long id) {
		String sql = """
				SELECT r.id, r.title, r.description, r.location_name, r.category_id, c.name AS category_name,
				       r.heritage_type_code,
				       r.copyright_declaration,
				       u.display_name AS contributor_name,
				       r.created_at, r.updated_at
				FROM resources r
				INNER JOIN categories c ON c.id = r.category_id
				LEFT JOIN heritage_users u ON r.submitter_id = u.id OR r.contributor_id = u.id
				WHERE r.status = 'APPROVED' AND r.id = :id
				LIMIT 1
				""";
		List<PublicResourceDetail> rows = jdbc.query(sql, Map.of("id", id), (rs, rowNum) -> mapDetail(rs));
		if (rows.isEmpty()) {
			return Optional.empty();
		}

		PublicResourceDetail detail = rows.get(0);
		String tagSql = """
				SELECT t.id, t.name
				FROM tags t
				INNER JOIN resource_tags rt ON rt.tag_id = t.id
				WHERE rt.resource_id = :resourceId
				ORDER BY t.name ASC
				""";
		List<NamedRow> tags = jdbc.query(tagSql, Map.of("resourceId", detail.getId()),
				(rs, row) -> new NamedRow(rs.getLong("id"), rs.getString("name")));
		detail.setTags(tags);

		String attachmentSql = """
				SELECT id, stored_name, display_name, file_path, file_type, file_size, thumbnail_path
				FROM attachments
				WHERE resource_id = :resourceId
				ORDER BY created_at ASC
				""";
		List<Map<String, Object>> attachments = jdbc.query(attachmentSql, Map.of("resourceId", detail.getId()), (rs, rowNum) -> {
			Map<String, Object> map = new java.util.HashMap<>();
			map.put("id", rs.getLong("id"));
			map.put("storedName", rs.getString("stored_name"));
			map.put("displayName", rs.getString("display_name"));
			map.put("filePath", rs.getString("file_path"));
			map.put("fileType", rs.getString("file_type"));
			map.put("fileSize", rs.getLong("file_size"));
			String thumbnailPath = rs.getString("thumbnail_path");
			if (thumbnailPath != null) {
				map.put("thumbnailUrl", "/api/attachments/" + rs.getLong("id") + "/thumbnail");
			}
			return map;
		});
		detail.setAttachments(attachments);

		return Optional.of(detail);
	}

	private PublicResourceDetail mapDetail(ResultSet rs) throws SQLException {
		PublicResourceDetail d = new PublicResourceDetail();
		d.setId(rs.getLong("id"));
		d.setTitle(rs.getString("title"));
		d.setDescription(rs.getString("description"));
		d.setLocationName(rs.getString("location_name"));
		d.setCategoryId(rs.getObject("category_id") != null ? rs.getInt("category_id") : null);
		d.setCategoryName(rs.getString("category_name"));
		d.setCopyrightDeclaration(rs.getString("copyright_declaration"));
		d.setContributorName(rs.getString("contributor_name"));
		d.setCreatedAt(toLocal(rs, "created_at"));
		d.setUpdatedAt(toLocal(rs, "updated_at"));
		enrichDerived(d, rs);
		return d;
	}

	private void enrichDerived(PublicResourceSummary s, ResultSet rs) throws SQLException {
		var ts = rs.getTimestamp("created_at");
		if (ts != null) {
			LocalDateTime ldt = ts.toLocalDateTime();
			int y = ldt.getYear();
			String dc = taxonomy.inferDynastyCodeFromGregorianYear(y);
			s.setDynastyCode(dc);
			s.setDynastyName(taxonomy.dynastyName(dc).orElse(null));
			LocalDate cd = ldt.toLocalDate();
			s.setEraStart(cd);
			s.setEraEnd(cd);
		}
		String loc = rs.getString("location_name");
		taxonomy.inferProvinceCodeFromLocation(loc).ifPresentOrElse(pc -> {
			s.setProvinceCode(pc);
			s.setProvinceName(taxonomy.provinceName(pc).orElse(null));
		}, () -> {
			s.setProvinceCode(null);
			s.setProvinceName(null);
		});
		String typeCode = rs.getString("heritage_type_code");
		s.setHeritageTypeCode(typeCode);
		s.setHeritageTypeLabel(taxonomy.heritageTypeLabel(typeCode).orElse(null));
	}

	private void enrichDerived(PublicResourceDetail d, ResultSet rs) throws SQLException {
		var ts = rs.getTimestamp("created_at");
		if (ts != null) {
			LocalDateTime ldt = ts.toLocalDateTime();
			int y = ldt.getYear();
			String dc = taxonomy.inferDynastyCodeFromGregorianYear(y);
			d.setDynastyCode(dc);
			d.setDynastyName(taxonomy.dynastyName(dc).orElse(null));
			LocalDate cd = ldt.toLocalDate();
			d.setEraStart(cd);
			d.setEraEnd(cd);
		}
		String loc = rs.getString("location_name");
		taxonomy.inferProvinceCodeFromLocation(loc).ifPresentOrElse(pc -> {
			d.setProvinceCode(pc);
			d.setProvinceName(taxonomy.provinceName(pc).orElse(null));
		}, () -> {
			d.setProvinceCode(null);
			d.setProvinceName(null);
		});
		String typeCode = rs.getString("heritage_type_code");
		d.setHeritageTypeCode(typeCode);
		d.setHeritageTypeLabel(taxonomy.heritageTypeLabel(typeCode).orElse(null));
	}

	private class SummaryMapper implements RowMapper<PublicResourceSummary> {
		@Override
		public PublicResourceSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
			PublicResourceSummary s = new PublicResourceSummary();
			s.setId(rs.getLong("id"));
			s.setTitle(rs.getString("title"));
			s.setDescription(rs.getString("description"));
			s.setLocationName(rs.getString("location_name"));
			s.setCategoryId(rs.getObject("category_id") != null ? rs.getInt("category_id") : null);
			s.setCategoryName(rs.getString("category_name"));
			s.setCreatedAt(toLocal(rs, "created_at"));
			s.setUpdatedAt(toLocal(rs, "updated_at"));
			try {
				s.setCoverUrl(rs.getString("cover_url"));
			} catch (SQLException ignored) {
				// cover_url is not selected in some queries
			}
			try {
				s.setHasVideo(rs.getBoolean("has_video"));
			} catch (SQLException ignored) {
				// has_video is not selected in some queries
			}
			try {
				s.setCommentCount(rs.getInt("comment_count"));
			} catch (SQLException ignored) {
				// comment_count is not selected in some queries
			}
			try {
				s.setLikeCount(rs.getInt("like_count"));
			} catch (SQLException ignored) {
				// like_count is not selected in some queries
			}
			try {
				s.setFavoriteCount(rs.getInt("favorite_count"));
			} catch (SQLException ignored) {
				// favorite_count is not selected in some queries
			}
			enrichDerived(s, rs);
			return s;
		}
	}

	private static LocalDateTime toLocal(ResultSet rs, String col) throws SQLException {
		var ts = rs.getTimestamp(col);
		if (ts == null) {
			return null;
		}
		return ts.toLocalDateTime();
	}

	/** 把逗号分隔的 id 列表解析成 Long 列表，非法片段直接跳过 */
	public static List<Long> parseTagIds(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<Long> out = new ArrayList<>();
		for (String part : raw.split(",")) {
			String t = part.trim();
			if (t.isEmpty()) {
				continue;
			}
			try {
				out.add(Long.parseLong(t));
			} catch (NumberFormatException ignored) {
				// 忽略坏片段，避免整次请求失败
			}
		}
		return out;
	}
}
