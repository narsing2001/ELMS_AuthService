package com.example.educationloan.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Flat DTO consumed by UserRolesReport.jrxml.
 *
 * Field names here MUST match the <field name="..."> entries in the JRXML exactly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleReportRow {

    // ── UserRole ───────────────────────────────
    private Long          id;           // UserRole PK
    private String        assignedBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "dd-MM-yy HH:mm")
    private LocalDateTime assignedAt;

    // ── User ───────────────────────────────────
    private Long          userId;
    private String        username;
    private String        firstName;
    private String        lastName;
    private String        email;
    private Boolean       isActive;

    // ── Role ───────────────────────────────────
    private Long          roleId;
    private String        roleName;     // RoleEnum.name()  e.g. "ROLE_ADMIN"
}
