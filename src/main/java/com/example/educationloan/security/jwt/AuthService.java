package com.example.educationloan.security.jwt;

import com.example.educationloan.config.JwtTokenProvider;
import com.example.educationloan.dto.AuthLogDTO;
import com.example.educationloan.dto.RegisterDTO;
import com.example.educationloan.entity.User;
import com.example.educationloan.exception.InvalidTokenException;
import com.example.educationloan.report.AuthLogStore;
import com.example.educationloan.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.educationloan.dto.AuthDTO;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthInterface {

    private final AuthenticationManager    authenticationManager;
    private final JwtTokenProvider         jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserService              userService;
    private final AuthLogStore authLogStore;
    //In-memory auth log (persists while app is running)
    private final List<AuthLogDTO> authLogs = Collections.synchronizedList(new ArrayList<>());

    // build AuthLogDTO and store it
    private void saveLog(AuthDTO response, String operation, boolean success) {
        authLogStore.add(AuthLogDTO.builder()
                .username(response.getUsername())
                .operation(operation)
                .tokenType(response.getTokenType())
                .accessExpiresAt(response.getAccessTokenExpiresAt() != null
                        ? response.getAccessTokenExpiresAt().toString() : "")
                .refreshExpiresAt(response.getRefreshTokenExpiresAt() != null
                        ? response.getRefreshTokenExpiresAt().toString() : "")
                .accessExpiresInSeconds(response.getAccessTokenExpiresInSeconds())
                .refreshExpiresInSeconds(response.getRefreshTokenExpiresInSeconds())
                .timestamp(LocalDateTime.now())
                .success(success)
                .build());
    }

    // convert token → AuthDTO with expiry fields
    private AuthDTO buildAuthDTO(String accessToken, String refreshToken, String username) {

        // 1. extract expiry Date directly from JWT claims
        Date accessExpDate  = jwtTokenProvider.extractExpiration(accessToken);
        Date refreshExpDate = jwtTokenProvider.extractExpiration(refreshToken);

        // 2. convert Date → LocalDateTime
        LocalDateTime accessExpiresAt  = accessExpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime refreshExpiresAt = refreshExpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 3. calculate seconds remaining from now
        long accessExpiresInSeconds  = ChronoUnit.SECONDS.between(LocalDateTime.now(), accessExpiresAt);
        long refreshExpiresInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), refreshExpiresAt);

        // 4. build and return AuthDTO with all expiry fields populated
        AuthDTO authDTO = new AuthDTO(accessToken, refreshToken, username);
        authDTO.setAccessTokenExpiresAt(accessExpiresAt);
        authDTO.setRefreshTokenExpiresAt(refreshExpiresAt);
        authDTO.setAccessTokenExpiresInSeconds(accessExpiresInSeconds);
        authDTO.setRefreshTokenExpiresInSeconds(refreshExpiresInSeconds);
        return authDTO;
    }



    public AuthDTO login(String usernameOrEmail, String password) {
        // This triggers CustomUserDetailsService + BCrypt password check
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken  = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        log.info("User '{}' logged in successfully", userDetails.getUsername());
        AuthDTO response = buildAuthDTO(accessToken, refreshToken, userDetails.getUsername());
        saveLog(response, "LOGIN", true);   // ← log saved
        //return buildAuthDTO(accessToken, refreshToken, userDetails.getUsername());
        return response;
    }

    public AuthDTO register(RegisterDTO request) {
        User newUser = userService.registerUser(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
        String accessToken  = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        log.info("User '{}' registered successfully", newUser.getUsername());
        AuthDTO response = buildAuthDTO(accessToken, refreshToken, newUser.getUsername());
        saveLog(response, "REGISTER", true);   // ← log saved
       // return buildAuthDTO(accessToken, refreshToken, newUser.getUsername());
        return response;
    }

    public AuthDTO refresh(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String      username    = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String      newAccess   = jwtTokenProvider.generateAccessToken(userDetails);

        log.info("Access token refreshed for user '{}'", username);
        AuthDTO response = buildAuthDTO(newAccess, refreshToken, username);
        saveLog(response, "REFRESH_TOKEN", true);   // ← log saved
        return response;
    }
}