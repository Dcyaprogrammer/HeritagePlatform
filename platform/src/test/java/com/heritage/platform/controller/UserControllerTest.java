package com.heritage.platform.controller;

import com.heritage.platform.dto.ApiResponse;
import com.heritage.platform.dto.ChangePasswordRequest;
import com.heritage.platform.dto.UserDTO;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private HeritageUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("更新用户角色：晋升为贡献者应自动更新状态为已批准")
    void updateUserRole_ToContributor_Success() {
        HeritageUser user = new HeritageUser();
        ReflectionTestUtils.setField(user, "id", 1L); 
        user.setUsername("YuhanMei");
        Set<String> initialRoles = new HashSet<>();
        initialRoles.add("VIEWER");
        user.setRoles(initialRoles);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(HeritageUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ApiResponse<UserDTO>> response = userController.updateUserRole(1L, "CONTRIBUTOR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO resultData = response.getBody().getData();
        assertTrue(resultData.getRoles().contains("CONTRIBUTOR"));
        assertEquals("APPROVED", resultData.getContributorStatus());
        verify(userRepository).save(any(HeritageUser.class));
    }

    @Test
    @DisplayName("修改密码：当旧密码不匹配时应返回错误提示")
    void changePassword_IncorrectOldPassword() {
        HeritageUser user = new HeritageUser();
        user.setPasswordHash("encoded_actual_password");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong_password");

        when(userRepository.findByUsername("YuhanMei")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "encoded_actual_password")).thenReturn(false);

        ResponseEntity<ApiResponse<?>> response = userController.changePassword("YuhanMei", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Old password is incorrect", response.getBody().getMessage());
    }
}