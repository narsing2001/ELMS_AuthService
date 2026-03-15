package com.example.educationloan.service;

import com.example.educationloan.entity.Role;
import com.example.educationloan.entity.User;
import com.example.educationloan.entity.UserRole;
import com.example.educationloan.exception.ResourceNotFoundException;
import com.example.educationloan.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleService implements UserRoleInterface{

    private final UserRoleRepository userRoleRepository;

    // Fetch all UserRole mappings for a given user
    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleRepository.findByUser_Id(userId);
    }

    // Fetch all UserRole mappings for a given role
    public List<UserRole> getUserRolesByRoleId(Long roleId) {
        return userRoleRepository.findByRole_RoleId(roleId);
    }


    // Remove a role assignment
    public void removeUserRole(Long userRoleId) {
        userRoleRepository.deleteById(userRoleId);
    }

    // Fetch user with roles eagerly
    public User getUserById(Long id) {
        return userRoleRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserRole giveRoleToUser(User user, Role role, String assignedBy) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        return userRoleRepository.save(userRole);
    }

    @Override
    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }
}