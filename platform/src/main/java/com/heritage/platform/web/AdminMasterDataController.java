package com.heritage.platform.web;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.model.Category;
import com.heritage.platform.model.Tag;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.TagRepository;

@RestController
@RequestMapping("/api/admin/master-data")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMasterDataController {

    private final CategoryRepository categories;
    private final TagRepository tags;

    public AdminMasterDataController(CategoryRepository categories, TagRepository tags) {
        this.categories = categories;
        this.tags = tags;
    }

    public static class CategoryUpsertRequest {
        public String name;
        public String description;
    }

    public static class TagUpsertRequest {
        public String name;
    }

    public static class CategoryRow {
        public Integer id;
        public String name;
        public String description;

        public CategoryRow(Integer id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    public static class TagRow {
        public Long id;
        public String name;

        public TagRow(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryRow>>> listCategories() {
        List<CategoryRow> rows = categories.findAll().stream()
                .map(c -> new CategoryRow(c.getId(), c.getName(), c.getDescription()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryRow>> createCategory(@RequestBody CategoryUpsertRequest request) {
        String name = request == null ? null : request.name;
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "name is required"));
        }
        String normalized = name.trim();
        if (categories.existsByNameIgnoreCase(normalized)) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "category name already exists"));
        }

        Category c = new Category();
        c.setName(normalized);
        c.setDescription(request == null ? null : request.description);
        Category saved = categories.save(c);
        return ResponseEntity.ok(ApiResponse.success(new CategoryRow(saved.getId(), saved.getName(), saved.getDescription())));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryRow>> updateCategory(
            @PathVariable Integer id,
            @RequestBody CategoryUpsertRequest request) {
        Category c = categories.findById(id).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "category not found"));
        }

        String name = request == null ? null : request.name;
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "name is required"));
        }
        String normalized = name.trim();
        Category existing = categories.findByNameIgnoreCase(normalized).orElse(null);
        if (existing != null && !existing.getId().equals(id)) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "category name already exists"));
        }

        c.setName(normalized);
        c.setDescription(request == null ? null : request.description);
        Category saved = categories.save(c);
        return ResponseEntity.ok(ApiResponse.success(new CategoryRow(saved.getId(), saved.getName(), saved.getDescription())));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Integer id) {
        if (!categories.existsById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "category not found"));
        }
        try {
            categories.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Deleted"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "category is referenced by resources"));
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagRow>>> listTags() {
        List<TagRow> rows = tags.findAll().stream()
                .map(t -> new TagRow(t.getId(), t.getName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagRow>> createTag(@RequestBody TagUpsertRequest request) {
        String name = request == null ? null : request.name;
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "name is required"));
        }
        String normalized = name.trim();
        if (tags.existsByNameIgnoreCase(normalized)) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "tag name already exists"));
        }

        Tag t = new Tag();
        t.setName(normalized);
        Tag saved = tags.save(t);
        return ResponseEntity.ok(ApiResponse.success(new TagRow(saved.getId(), saved.getName())));
    }

    @PutMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<TagRow>> updateTag(@PathVariable Long id, @RequestBody TagUpsertRequest request) {
        Tag t = tags.findById(id).orElse(null);
        if (t == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "tag not found"));
        }
        String name = request == null ? null : request.name;
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "name is required"));
        }
        String normalized = name.trim();
        Tag existing = tags.findByNameIgnoreCase(normalized).orElse(null);
        if (existing != null && !existing.getId().equals(id)) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "tag name already exists"));
        }

        t.setName(normalized);
        Tag saved = tags.save(t);
        return ResponseEntity.ok(ApiResponse.success(new TagRow(saved.getId(), saved.getName())));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTag(@PathVariable Long id) {
        if (!tags.existsById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "tag not found"));
        }
        try {
            tags.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Deleted"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "tag is referenced by resources"));
        }
    }
}

