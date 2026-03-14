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


@Service
public class JasperReportService {

    //1. USER LIST REPORT
    public byte[] generateUserListReport(List<UserDTO> users, String generatedBy) throws JRException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);
        return fill("reports/UserListReport.jrxml", params, dataSource);
    }

    // 2. USER ROLES REPORT
    public byte[] generateUserRolesReport(List<?> userRoleRows, String generatedBy, String filterRole) throws JRException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);
        params.put("FILTER_ROLE", filterRole != null ? filterRole : "ALL");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(userRoleRows);
        return fill("reports/UserRolesReport.jrxml", params, dataSource);
    }

    // 3. AUTH SUMMARY REPORT
    public byte[] generateAuthSummaryReport(List<?> authLogs,String generatedBy, String fromDate,String toDate) throws JRException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_GENERATED_BY", generatedBy);
        params.put("FROM_DATE", fromDate != null ? fromDate : "–");
        params.put("TO_DATE",   toDate   != null ? toDate   : "–");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(authLogs);
        return fill("reports/AuthSummaryReport.jrxml", params, dataSource);
    }

    private byte[] fill(String classpathResource, Map<String, Object> params,JRDataSource dataSource) throws JRException, IOException {
        try (InputStream is = new ClassPathResource(classpathResource).getInputStream()) {
            JasperReport compiled = JasperCompileManager.compileReport(is);
            JasperPrint  print    = JasperFillManager.fillReport(compiled, params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        }
    }
}
