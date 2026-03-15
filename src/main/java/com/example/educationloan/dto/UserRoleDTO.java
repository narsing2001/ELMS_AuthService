package com.example.educationloan.dto;


import com.example.educationloan.entity.UserRole;
import com.example.educationloan.enumconstant.RoleEnum;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDTO {
    private Long id;
    private Long userId;
    private Long roleId;
    private RoleEnum roleName;
    private String assignedBy;

    // ADD these two fields
//    private String username;
//    private String name;

    public static UserRoleDTO fromEntity(UserRole userRole) {
        return new UserRoleDTO(
                userRole.getId(),
                userRole.getUser().getId(),
                userRole.getRole().getRoleId(),
                userRole.getRole().getName(),
                userRole.getAssignedBy()
        );
    }


}
