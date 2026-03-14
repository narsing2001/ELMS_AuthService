package com.example.educationloan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogDTO {
    private String        username;
    private String        operation;
    private String        tokenType;
    private String        accessExpiresAt;
    private String        refreshExpiresAt;
    private Long          accessExpiresInSeconds;
    private Long          refreshExpiresInSeconds;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "dd-MM-yy HH:mm")
    private LocalDateTime timestamp;
    private String        ipAddress;
    private Boolean       success;
}