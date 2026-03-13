package com.example.educationloan.controller;

import com.example.educationloan.dto.RefreshRequestDTO;
import com.example.educationloan.security.jwt.AuthService;
import com.example.educationloan.dto.AuthDTO;
import com.example.educationloan.dto.LoginDTO;
import com.example.educationloan.dto.RegisterDTO;
import com.example.educationloan.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private static final String ROW = "+---------------------------+----------------------------------+";
    private void logAuthTable(String operation, AuthDTO r) {
        log.info("Operation   : {}", operation);
        log.info(ROW);
        log.info(String.format("| %-25s | %-32s |", "Field", "Value"));
        log.info(ROW);
        log.info(String.format("| %-25s | %-32s |", "Username",                r.getUsername()));
        log.info(String.format("| %-25s | %-32s |", "Token Type",              r.getTokenType()));
        log.info(String.format("| %-25s | %-32s |", "Access Token",            "abstract"));
        log.info(String.format("| %-25s | %-32s |", "Refresh Token",           "abstract"));
        log.info(String.format("| %-25s | %-32s |", "Access  Expires At",      r.getAccessTokenExpiresAt()));
        log.info(String.format("| %-25s | %-32s |", "Access  Expires In",      r.getAccessTokenExpiresInSeconds()  + " sec"));
        log.info(String.format("| %-25s | %-32s |", "Refresh Expires At",      r.getRefreshTokenExpiresAt()));
        log.info(String.format("| %-25s | %-32s |", "Refresh Expires In",      r.getRefreshTokenExpiresInSeconds() + " sec"));
        log.info(ROW);
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDTO>> register(@Valid @RequestBody RegisterDTO request) {
        log.info("REQUEST  : POST /api/v1/auth/register | username={}", request.getFirstName());
        AuthDTO response = authService.register(request);
        logAuthTable("REGISTER", response);
        log.info("RESPONSE : 200 OK | username={} | tokenType={} | accessExpiresIn={}sec | refreshExpiresIn={}sec",
                response.getUsername(), response.getTokenType(),
                response.getAccessTokenExpiresInSeconds(),
                response.getRefreshTokenExpiresInSeconds());
        return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTO>> login(@Valid @RequestBody LoginDTO request) {
        log.info("REQUEST  : POST /api/v1/auth/login | usernameOrEmail={}", request.getUsernameOrEmail());
        AuthDTO response = authService.login(request.getUsernameOrEmail(), request.getPassword());
        logAuthTable("LOGIN", response);
        log.info("RESPONSE : 200 OK | username={} | tokenType={} | accessExpiresIn={}sec | refreshExpiresIn={}sec",
                response.getUsername(), response.getTokenType(),
                response.getAccessTokenExpiresInSeconds(),
                response.getRefreshTokenExpiresInSeconds());
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDTO>> refresh(@RequestParam String refreshToken) {
        log.info("REQUEST  : POST /api/v1/auth/refresh | Token refresh initiated");
        AuthDTO response = authService.refresh(refreshToken);
        logAuthTable("REFRESH_TOKEN", response);
        log.info("RESPONSE : 200 OK | username={} | tokenType={} | newAccessExpiresIn={}sec | refreshExpiresIn={}sec",
                response.getUsername(), response.getTokenType(),
                response.getAccessTokenExpiresInSeconds(),
                response.getRefreshTokenExpiresInSeconds());
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", response));
    }

    @PostMapping("/refresh1")
    public ResponseEntity<ApiResponse<AuthDTO>> refresh(@RequestBody RefreshRequestDTO request) {
        log.info("REQUEST  : POST /api/v1/auth/refresh | Token refresh initiated");
        AuthDTO response = authService.refresh(request.getRefreshToken());
        logAuthTable("REFRESH_TOKEN", response);
        log.info("RESPONSE : 200 OK | username={} | tokenType={} | newAccessExpiresIn={}sec | refreshExpiresIn={}sec",
                response.getUsername(), response.getTokenType(),
                response.getAccessTokenExpiresInSeconds(),
                response.getRefreshTokenExpiresInSeconds());
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", response));
    }

}