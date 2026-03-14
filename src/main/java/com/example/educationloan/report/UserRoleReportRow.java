package com.example.educationloan.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleReportRow {


    private Long          id;
    private String        assignedBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "dd-MM-yy HH:mm")
    private LocalDateTime assignedAt;


    private Long          userId;
    private String        username;
    private String        firstName;
    private String        lastName;
    private String        email;
    private Boolean       isActive;

    private Long          roleId;
    private String        roleName;
}
