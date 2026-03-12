package com.example.educationloan.report;

import com.example.educationloan.dto.UserDTO;
import com.example.educationloan.dto.UserRoleDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JasperReportService
 *
 * Compiles and fills the three JRXML reports for the Education Loan Auth Service.
 *
 * Place the three .jrxml files in:
 *   src/main/resources/reports/
 *     ├── UserListReport.jrxml
 *     ├── UserRolesReport.jrxml
 *     └── AuthSummaryReport.jrxml
 *
 * Maven / Gradle dependency required:
 *   <dependency>
 *       <groupId>net.sf.jasperreports</groupId>
 *       <artifactId>jasperreports</artifactId>
 *       <version>6.21.0</version>
 *   </dependency>
 */
@Service
public class JasperReportService {

    // ── 1. USER LIST REPORT ──────────────────────────────────────────────────
    /**
     * Generates a PDF byte array of all users.
     *
     * Data source: List<UserDTO>
     *   Required fields on UserDTO:
     *     id, username, firstName, lastName, email,
     *     isActive, isEmailVerified, createdAt, updatedAt
     */
    public byte[] generateUserListReport(List<UserDTO> users, String generatedBy)
            throws JRException, IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);
        return fill("reports/UserListReport.jrxml", params, dataSource);
    }

    // ── 2. USER ROLES REPORT ─────────────────────────────────────────────────
    /**
     * Generates a PDF byte array of all user-role mappings.
     *
     * Data source: List<UserRoleReportRow>
     *   Required fields:
     *     id (UserRole PK), userId, username, firstName, lastName, email,
     *     isActive, roleId, roleName, assignedBy, assignedAt
     *
     * Build the list like this in your controller/service:
     *
     *   List<UserRoleReportRow> rows = userRoleService.findAll().stream()
     *       .map(ur -> UserRoleReportRow.builder()
     *           .id(ur.getId())
     *           .userId(ur.getUser().getId())
     *           .username(ur.getUser().getUsername())
     *           .firstName(ur.getUser().getFirstName())
     *           .lastName(ur.getUser().getLastName())
     *           .email(ur.getUser().getEmail())
     *           .isActive(ur.getUser().getIsActive())
     *           .roleId(ur.getRole().getRoleId())
     *           .roleName(ur.getRole().getName().name())  // RoleEnum → String
     *           .assignedBy(ur.getAssignedBy())
     *           .assignedAt(ur.getAssignedAt())
     *           .build())
     *       .toList();
     */
    public byte[] generateUserRolesReport(List<?> userRoleRows,
                                          String generatedBy,
                                          String filterRole)
            throws JRException, IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);
        params.put("FILTER_ROLE", filterRole != null ? filterRole : "ALL");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(userRoleRows);
        return fill("reports/UserRolesReport.jrxml", params, dataSource);
    }

    // ── 3. AUTH SUMMARY REPORT ───────────────────────────────────────────────
    /**
     * Generates a PDF byte array of auth operations (register / login / refresh).
     *
     * Data source: List<AuthLogDTO>
     *   Required fields:
     *     username, operation (REGISTER | LOGIN | REFRESH_TOKEN),
     *     tokenType, accessExpiresAt, refreshExpiresAt,
     *     accessExpiresInSeconds, refreshExpiresInSeconds,
     *     timestamp (LocalDateTime), ipAddress, success (Boolean)
     *
     * You can persist auth events in an audit table and query them,
     * or build the list in-memory inside AuthService after each operation.
     */
    public byte[] generateAuthSummaryReport(List<?> authLogs,
                                            String generatedBy,
                                            String fromDate,
                                            String toDate)
            throws JRException, IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);
        params.put("FROM_DATE", fromDate != null ? fromDate : "–");
        params.put("TO_DATE",   toDate   != null ? toDate   : "–");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(authLogs);
        return fill("reports/AuthSummaryReport.jrxml", params, dataSource);
    }

    // ── Internal helper ──────────────────────────────────────────────────────
    private byte[] fill(String classpathResource,
                        Map<String, Object> params,
                        JRDataSource dataSource)
            throws JRException, IOException {

        try (InputStream is = new ClassPathResource(classpathResource).getInputStream()) {
            JasperReport compiled = JasperCompileManager.compileReport(is);
            JasperPrint  print    = JasperFillManager.fillReport(compiled, params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        }
    }
}
