package com.heritage.platform.service;

import com.heritage.platform.discovery.dto.NamedRow;
import com.heritage.platform.discovery.dto.PublicResourceSummary;
import com.heritage.platform.discovery.dto.SlicePage;
import com.heritage.platform.model.Favorite;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.Like;
import com.heritage.platform.repository.FavoriteRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InteractionService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private HeritageResourceRepository resourceRepository;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Transactional
    public Map<String, Object> toggleLike(Long resourceId, String username) {
        HeritageResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        HeritageUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean liked;
        if (likeRepository.existsByUserIdAndResourceId(user.getId(), resourceId)) {
            likeRepository.findByUserIdAndResourceId(user.getId(), resourceId)
                    .ifPresent(likeRepository::delete);
            liked = false;
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setResource(resource);
            likeRepository.save(like);
            liked = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeRepository.countByResourceId(resourceId));
        return result;
    }

    @Transactional
    public Map<String, Object> toggleFavorite(Long resourceId, String username) {
        HeritageResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        HeritageUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean favorited;
        if (favoriteRepository.existsByUserIdAndResourceId(user.getId(), resourceId)) {
            favoriteRepository.findByUserIdAndResourceId(user.getId(), resourceId)
                    .ifPresent(favoriteRepository::delete);
            favorited = false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setResource(resource);
            favoriteRepository.save(favorite);
            favorited = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("favoriteCount", favoriteRepository.countByResourceId(resourceId));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInteractionStatus(Long resourceId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", likeRepository.countByResourceId(resourceId));
        result.put("favoriteCount", favoriteRepository.countByResourceId(resourceId));

        if (userId != null) {
            result.put("liked", likeRepository.existsByUserIdAndResourceId(userId, resourceId));
            result.put("favorited", favoriteRepository.existsByUserIdAndResourceId(userId, resourceId));
        } else {
            result.put("liked", false);
            result.put("favorited", false);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> getUserLikes(List<Long> resourceIds, Long userId) {
        if (resourceIds == null || resourceIds.isEmpty() || userId == null) {
            return Map.of();
        }
        List<Long> likedIds = likeRepository.findResourceIdsByUserId(userId);
        return resourceIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> likedIds.contains(id)
                ));
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> getUserFavorites(List<Long> resourceIds, Long userId) {
        if (resourceIds == null || resourceIds.isEmpty() || userId == null) {
            return Map.of();
        }
        List<Long> favoritedIds = favoriteRepository.findResourceIdsByUserId(userId);
        return resourceIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> favoritedIds.contains(id)
                ));
    }

    public long getLikeCount(Long resourceId) {
        return likeRepository.countByResourceId(resourceId);
    }

	public long getFavoriteCount(Long resourceId) {
		return favoriteRepository.countByResourceId(resourceId);
	}

	@Transactional(readOnly = true)
	public SlicePage<PublicResourceSummary> getUserFavorites(String username, int page, int size) {
		HeritageUser user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found: " + username));

		if (page < 0) page = 0;
		if (size < 1) size = 12;
		if (size > 100) size = 100;

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("userId", user.getId());

		String countSql = "SELECT COUNT(*) FROM favorites f WHERE f.user_id = :userId";
		Long total = jdbc.queryForObject(countSql, params, Long.class);
		if (total == null) total = 0L;

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
				       COALESCE((SELECT COUNT(*) FROM favorites fv WHERE fv.resource_id = r.id), 0) AS favorite_count
				FROM favorites fv
				INNER JOIN resources r ON r.id = fv.resource_id
				INNER JOIN categories c ON c.id = r.category_id
				WHERE fv.user_id = :userId
				ORDER BY fv.created_at DESC
				LIMIT :limit OFFSET :offset
				""";

		List<PublicResourceSummary> rows = jdbc.query(listSql, params, new FavoritesSummaryMapper());
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

	private class FavoritesSummaryMapper implements RowMapper<PublicResourceSummary> {
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

		private void enrichDerived(PublicResourceSummary s, ResultSet rs) throws SQLException {
			var ts = rs.getTimestamp("created_at");
			if (ts != null) {
				LocalDateTime ldt = ts.toLocalDateTime();
				s.setCreatedAt(ldt);
				s.setUpdatedAt(ldt);
			}
		}
	}

	private LocalDateTime toLocal(ResultSet rs, String col) throws SQLException {
		var ts = rs.getTimestamp(col);
		if (ts == null) {
			return null;
		}
		return ts.toLocalDateTime();
	}
}
