package com.example.educationloan.service;

import com.example.educationloan.entity.Role;
import com.example.educationloan.entity.User;
import com.example.educationloan.entity.UserRole;

import java.util.List;

public interface UserRoleInterface {

    List<UserRole> getUserRolesByUserId(Long userId);
    List<UserRole> getUserRolesByRoleId(Long roleId);
    User getUserById(Long id);
    UserRole giveRoleToUser(User user, Role role, String assignedBy);
    void removeUserRole(Long userRoleId);
    List<UserRole> getAllUserRoles();
}