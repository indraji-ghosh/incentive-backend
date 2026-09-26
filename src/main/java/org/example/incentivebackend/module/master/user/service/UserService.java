package org.example.incentivebackend.module.master.user.service;

import org.example.incentivebackend.module.master.user.dto.UserRequestDTO;
import org.example.incentivebackend.module.master.user.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(Long id, UserRequestDTO request);
    void deleteUser(Long id);
}
