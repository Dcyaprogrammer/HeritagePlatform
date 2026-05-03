package com.heritage.platform.web;

import com.heritage.platform.model.Category;
import com.heritage.platform.model.Tag;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMasterDataControllerTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private AdminMasterDataController controller;

    @Test
    @DisplayName("创建分类：成功流程")
    void testCreateCategory_Success() {
        AdminMasterDataController.CategoryUpsertRequest req = new AdminMasterDataController.CategoryUpsertRequest();
        req.name = " 瓷器 "; // 测试首尾空格修剪
        req.description = "精品瓷器";

        when(categoryRepository.existsByNameIgnoreCase("瓷器")).thenReturn(false);
        Category saved = new Category();
        saved.setId(1);
        saved.setName("瓷器");
        when(categoryRepository.save(any())).thenReturn(saved);

        var response = controller.createCategory(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("瓷器", response.getBody().getData().name);
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("创建分类：重名冲突校验")
    void testCreateCategory_Conflict() {
        AdminMasterDataController.CategoryUpsertRequest req = new AdminMasterDataController.CategoryUpsertRequest();
        req.name = "Existing";

        when(categoryRepository.existsByNameIgnoreCase("Existing")).thenReturn(true);

        var response = controller.createCategory(req);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("删除标签：处理外键约束异常")
    void testDeleteTag_DataIntegrityViolation() {
        Long tagId = 100L;
        when(tagRepository.existsById(tagId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Referenced"))
            .when(tagRepository).deleteById(tagId);

        var response = controller.deleteTag(tagId);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("referenced by resources"));
    }
}