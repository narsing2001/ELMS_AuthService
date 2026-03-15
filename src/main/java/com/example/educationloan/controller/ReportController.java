package com.example.educationloan.controller;

import com.example.educationloan.dto.AuthLogDTO;
import com.example.educationloan.dto.UserDTO;
import com.example.educationloan.entity.User;
import com.example.educationloan.entity.UserRole;
import com.example.educationloan.report.AuthLogStore;
import com.example.educationloan.report.JasperReportService;
import com.example.educationloan.report.UserRoleReportRow;
import com.example.educationloan.security.jwt.AuthService;
import com.example.educationloan.service.UserRoleService;
import com.example.educationloan.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final JasperReportService reportService;
    private final UserService         userService;
    private final UserRoleService     userRoleService;
    private final AuthService authService;   // ← real auth logs
    private final AuthLogStore authLogStore;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── 1. User List ──────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<byte[]> downloadUserListReport(@RequestParam(defaultValue = "ADMIN") String generatedBy) {
        log.info("REQUEST  : GET /api/v1/reports/users | generatedBy={}", generatedBy);
        try {
            List<User>    users    = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream().map(UserDTO::toUserDTO).collect(Collectors.toList());
            byte[] pdf = reportService.generateUserListReport(userDTOs, generatedBy);
            log.info("RESPONSE : 200 OK | UserListReport generated | records={}", userDTOs.size());
            return pdfResponse(pdf, "UserListReport.pdf");
        } catch (Exception e) {
            log.error("ERROR generating UserListReport: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── 2. User-Roles ────────────────────────────────────────────────────────
    @GetMapping("/user-roles")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadUserRolesReport(
            @RequestParam(defaultValue = "System") String generatedBy,
            @RequestParam(defaultValue = "ALL")    String roleFilter) {

        log.info("REQUEST  : GET /api/v1/reports/user-roles | generatedBy={} roleFilter={}", generatedBy, roleFilter);

        try {
            List<UserRole> userRoles = userRoleService.getAllUserRoles();
            List<UserRoleReportRow> rows = userRoles.stream()
                    .filter(ur -> "ALL".equalsIgnoreCase(roleFilter)
                                  || ur.getRole().getName().name().equalsIgnoreCase(roleFilter))
                    .map(ur -> UserRoleReportRow.builder()
                            .id(ur.getId())
                            .userId(ur.getUser().getId())
                            .username(ur.getUser().getUsername())
                            .firstName(ur.getUser().getFirstName())
                            .lastName(ur.getUser().getLastName())
                            .email(ur.getUser().getEmail())
                            .isActive(ur.getUser().getIsActive())
                            .roleId(ur.getRole().getRoleId())
                            .roleName(ur.getRole().getName().name())
                            .assignedBy(ur.getAssignedBy())
                            .assignedAt(ur.getAssignedAt())
                            .build())
                    .collect(Collectors.toList());
            byte[] pdf = reportService.generateUserRolesReport(rows, generatedBy, roleFilter);
            log.info("RESPONSE : 200 OK | UserRolesReport generated | records={}", rows.size());
            return pdfResponse(pdf, "UserRolesReport.pdf");
        } catch (Exception e) {
            log.error("ERROR generating UserRolesReport: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/auth-summary")
    public ResponseEntity<byte[]> downloadAuthSummaryReport(
            @RequestParam(defaultValue = "System") String generatedBy,
            @RequestParam(defaultValue = "–")      String fromDate,
            @RequestParam(defaultValue = "–")      String toDate) {
        log.info("REQUEST  : GET /api/v1/reports/auth-summary | generatedBy={} from={} to={}",
                generatedBy, fromDate, toDate);
        try {
            List<AuthLogDTO> authLogs = authLogStore.getAll();
            byte[] pdf = reportService.generateAuthSummaryReport(authLogs, generatedBy, fromDate, toDate);
            log.info("RESPONSE : 200 OK | AuthSummaryReport generated | records={}", authLogs.size());
            return pdfResponse(pdf, "AuthSummaryReport.pdf");

        } catch (Exception e) {
            log.error("ERROR generating AuthSummaryReport: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdf.length))
                .body(pdf);
    }



// ── JSON for live HTML preview ────────────────────────────────────────────

    @GetMapping("/auth-summary-data")
    public ResponseEntity<List<AuthLogDTO>> getAuthSummaryData() {
        List<AuthLogDTO> logs = authLogStore.getAll();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/users-data")
    public ResponseEntity<List<UserDTO>> getUsersData() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserDTO::toUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user-roles-data")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserRoleReportRow>> getUserRolesData(
            @RequestParam(defaultValue = "ALL") String roleFilter) {

        List<UserRoleReportRow> rows = userRoleService.getAllUserRoles()
                .stream()
                .filter(ur -> "ALL".equalsIgnoreCase(roleFilter)
                        || ur.getRole().getName().name().equalsIgnoreCase(roleFilter))
                .map(ur -> UserRoleReportRow.builder()
                        .id(ur.getId())
                        .userId(ur.getUser().getId())
                        .username(ur.getUser().getUsername())
                        .firstName(ur.getUser().getFirstName())
                        .lastName(ur.getUser().getLastName())
                        .email(ur.getUser().getEmail())
                        .isActive(ur.getUser().getIsActive())
                        .roleId(ur.getRole().getRoleId())
                        .roleName(ur.getRole().getName().name())
                        .assignedBy(ur.getAssignedBy())
                        .assignedAt(ur.getAssignedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    // ── ADD THIS NEW ENDPOINT ─────────────────────────────────────
    @GetMapping("/open")
    public void openReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        response.sendRedirect("/Report-API.html?token=" + token);
    }


}
