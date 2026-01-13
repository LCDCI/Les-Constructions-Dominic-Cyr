package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import org.springframework. jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AnalyticsReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AnalyticsReport> rowMapper = (rs, rowNum) -> {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(rs.getLong("report_id"));
        report.setReportIdentifier(rs.getString("report_identifier"));
        report.setReportName(rs.getString("report_name"));
        report.setReportType(rs.getString("report_type"));
        report.setFileFormat(rs.getString("file_format"));
        report.setStartDate(rs.getDate("start_date").toLocalDate());
        report.setEndDate(rs.getDate("end_date").toLocalDate());
        report.setGeneratedBy(rs.getString("generated_by"));
        report.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
        report.setFilePath(rs.getString("file_path"));
        report.setFileIdentifier(rs.getString("file_identifier"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return report;
    };

    public AnalyticsReport save(AnalyticsReport report) {
        String sql = "INSERT INTO analytics_reports (report_identifier, report_name, report_type, " +
                "file_format, start_date, end_date, generated_by, generated_at, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, report.getReportIdentifier(), report.getReportName(),
                report.getReportType(), report.getFileFormat(), report.getStartDate(),
                report.getEndDate(), report.getGeneratedBy(), report.getGeneratedAt(),
                report.getStatus());

        return findByReportIdentifier(report. getReportIdentifier()).orElse(report);
    }

    public Optional<AnalyticsReport> findByReportIdentifier(String reportIdentifier) {
        String sql = "SELECT * FROM analytics_reports WHERE report_identifier = ?";
        List<AnalyticsReport> results = jdbcTemplate.query(sql, rowMapper, reportIdentifier);
        return results.isEmpty() ? Optional.empty() : Optional.of(results. get(0));
    }

    public List<AnalyticsReport> findByGeneratedBy(String generatedBy) {
        String sql = "SELECT * FROM analytics_reports WHERE generated_by = ?  ORDER BY generated_at DESC";
        return jdbcTemplate.query(sql, rowMapper, generatedBy);
    }

    public List<AnalyticsReport> findAll() {
        String sql = "SELECT * FROM analytics_reports ORDER BY generated_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void updateStatus(String reportIdentifier, String status) {
        String sql = "UPDATE analytics_reports SET status = ? WHERE report_identifier = ?";
        jdbcTemplate.update(sql, status, reportIdentifier);
    }

    public void updateFileIdentifier(String reportIdentifier, String fileIdentifier) {
        String sql = "UPDATE analytics_reports SET file_identifier = ? WHERE report_identifier = ?";
        jdbcTemplate.update(sql, fileIdentifier, reportIdentifier);
    }
}
