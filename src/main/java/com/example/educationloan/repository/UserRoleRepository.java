package com.example.educationloan.repository;

import com.example.educationloan.dto.UserRoleDTO;
import com.example.educationloan.entity.User;
import com.example.educationloan.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // Fetch all UserRole mappings for a given user
    List<UserRole> findByUser_Id(Long userId);

    // Fetch all UserRole mappings for a given role
    List<UserRole> findByRole_RoleId(Long roleId);

    //Fetch User with all roles eagerly — fixes LazyInitializationException(fixes N+1) problem
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    List<UserRole> findByUserId(Long userId);

    //Explicit JPQL query to find according to userid and role-id----------------------------------------
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    Optional<UserRole> findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);



    @Query("SELECT new com.example.educationloan.dto.UserRoleDTO(u.id, u.id, r.roleId, r.name, ur.assignedBy) " +
            "FROM User u JOIN u.userRoles ur JOIN ur.role r")
    List<UserRoleDTO> findUserRolesData();




}