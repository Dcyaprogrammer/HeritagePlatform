package com.heritage.platform.web;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.repository.HeritageResourceRepository;

@RestController
@RequestMapping("/api/admin/resources")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResourceController {

    private final NamedParameterJdbcTemplate jdbc;
    private final HeritageResourceRepository resources;

    public AdminResourceController(NamedParameterJdbcTemplate jdbc, HeritageResourceRepository resources) {
        this.jdbc = jdbc;
        this.resources = resources;
    }

    public static class AdminResourceItem {
        public Long id;
        public String title;
        public String status;
        public Integer categoryId;
        public String categoryName;
        public Instant submittedAt;
        public Instant updatedAt;
        public Long version;
        public String submitterName;
    }

    public static class PageResponse<T> {
        public List<T> items;
        public long total;
        public int page;
        public int size;

        public PageResponse(List<T> items, long total, int page, int size) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }

    public static class VersionRequest {
        public Long version;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminResourceItem>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        String statusNorm = null;
        if (status != null && !status.isBlank()) {
            String candidate = status.trim().toUpperCase();
            try {
                ResourceStatus.valueOf(candidate);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "invalid status"));
            }
            statusNorm = candidate;
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        if (statusNorm != null) {
            where.append(" AND r.status = :status ");
            params.addValue("status", statusNorm);
        }
        if (q != null && !q.isBlank()) {
            where.append(" AND r.title LIKE :kw ");
            params.addValue("kw", "%" + q.trim() + "%");
        }
        if (categoryId != null) {
            where.append(" AND r.category_id = :categoryId ");
            params.addValue("categoryId", categoryId);
        }

        String from = """
                FROM resources r
                INNER JOIN categories c ON c.id = r.category_id
                LEFT JOIN heritage_users u ON r.submitter_id = u.id OR r.contributor_id = u.id
                """;

        String countSql = "SELECT COUNT(*) " + from + where;
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", size);
        params.addValue("offset", page * size);

        String listSql = """
                SELECT r.id, r.title, r.status, r.category_id, c.name AS category_name,
                       r.submitted_at, r.updated_at, r.version,
                       COALESCE(u.display_name, u.username) AS submitter_name
                """
                + from
                + where
                + """
                ORDER BY r.updated_at DESC
                LIMIT :limit OFFSET :offset
                """;

        List<AdminResourceItem> items = jdbc.query(listSql, params, new AdminResourceMapper());
        return ResponseEntity.ok(ApiResponse.success(new PageResponse<>(items, total, page, size)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<String>> archive(@PathVariable Long id, @RequestBody VersionRequest request) {
        try {
            setStatusWithVersion(id, request == null ? null : request.version, ResourceStatus.APPROVED, ResourceStatus.ARCHIVED);
            return ResponseEntity.ok(ApiResponse.success("Archived"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Resource not found"));
        } catch (ConflictException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "Version mismatch, please refresh"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<String>> restore(@PathVariable Long id, @RequestBody VersionRequest request) {
        try {
            setStatusWithVersion(id, request == null ? null : request.version, ResourceStatus.ARCHIVED, ResourceStatus.APPROVED);
            return ResponseEntity.ok(ApiResponse.success("Restored"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Resource not found"));
        } catch (ConflictException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "Version mismatch, please refresh"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    private void setStatusWithVersion(Long id, Long version, ResourceStatus from, ResourceStatus to) {
        HeritageResource r = resources.findById(id).orElseThrow(() -> new NoSuchElementException("Resource not found"));

        if (r.getStatus() != from) {
            throw new IllegalArgumentException("invalid status transition");
        }
        if (version == null || r.getVersion() == null || !version.equals(r.getVersion())) {
            throw new ConflictException("Version mismatch");
        }

        r.setStatus(to);
        resources.save(r);
    }

    private static class ConflictException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ConflictException(String msg) {
            super(msg);
        }
    }

    private static class AdminResourceMapper implements RowMapper<AdminResourceItem> {
        @Override
        public AdminResourceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminResourceItem item = new AdminResourceItem();
            item.id = rs.getLong("id");
            item.title = rs.getString("title");
            item.status = rs.getString("status");
            Object cid = rs.getObject("category_id");
            item.categoryId = cid == null ? null : rs.getInt("category_id");
            item.categoryName = rs.getString("category_name");
            item.submittedAt = toInstant(rs, "submitted_at");
            item.updatedAt = toInstant(rs, "updated_at");
            Object v = rs.getObject("version");
            item.version = v == null ? null : rs.getLong("version");
            item.submitterName = rs.getString("submitter_name");
            return item;
        }
    }

    private static Instant toInstant(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        if (ts == null) {
            return null;
        }
        LocalDateTime ldt = ts.toLocalDateTime();
        return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant();
    }
}

