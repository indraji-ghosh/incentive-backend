package org.example.incentivebackend.module.master.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.constant.ApiConstants;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.user.dto.UserRequestDTO;
import org.example.incentivebackend.module.master.user.dto.UserResponseDTO;
import org.example.incentivebackend.module.master.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/master/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'USER_MASTER', 'VIEW')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> data = userService.getAllUsers();
        return ResponseBuilder.list("Users", data);
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'USER_MASTER', 'ADD')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@RequestBody UserRequestDTO request) {
        UserResponseDTO data = userService.createUser(request);
        return ResponseBuilder.created("User", data);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'USER_MASTER', 'UPDATE')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id, @RequestBody UserRequestDTO request) {
        UserResponseDTO data = userService.updateUser(id, request);
        return ResponseBuilder.updated("User", data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'USER_MASTER', 'DELETE')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseBuilder.deleted("User");
    }
}
