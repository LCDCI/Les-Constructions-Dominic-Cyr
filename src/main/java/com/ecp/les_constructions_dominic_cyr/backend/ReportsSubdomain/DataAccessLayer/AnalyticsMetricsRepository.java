package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework. jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AnalyticsMetricsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsMetricsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AnalyticsMetric> rowMapper = (rs, rowNum) -> {
        AnalyticsMetric metric = new AnalyticsMetric();
        metric.setMetricId(rs.getLong("metric_id"));
        metric.setMetricIdentifier(rs.getString("metric_identifier"));
        metric.setMetricDate(rs.getDate("metric_date").toLocalDate());
        metric.setUniqueVisitors(rs.getInt("unique_visitors"));
        metric.setTotalSessions(rs.getInt("total_sessions"));
        metric.setTotalPageviews(rs.getInt("total_pageviews"));
        metric.setNewVisitors(rs.getInt("new_visitors"));
        metric.setReturningVisitors(rs.getInt("returning_visitors"));
        metric.setBounceRate(rs.getBigDecimal("bounce_rate"));
        metric.setAvgSessionDuration(rs.getBigDecimal("avg_session_duration"));
        metric.setPagesPerSession(rs.getBigDecimal("pages_per_session"));
        metric.setConversionRate(rs.getBigDecimal("conversion_rate"));
        metric.setGoalCompletions(rs.getInt("goal_completions"));
        metric.setOrganicTraffic(rs.getInt("organic_traffic"));
        metric.setPaidTraffic(rs. getInt("paid_traffic"));
        metric.setSocialTraffic(rs.getInt("social_traffic"));
        metric.setDirectTraffic(rs.getInt("direct_traffic"));
        metric.setAvgPageLoadTime(rs.getBigDecimal("avg_page_load_time"));
        metric.setErrorCount(rs.getInt("error_count"));
        metric.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        metric.setUpdatedAt(rs. getTimestamp("updated_at").toLocalDateTime());
        return metric;
    };

    public AnalyticsMetric save(AnalyticsMetric metric) {
        String sql = "INSERT INTO analytics_metrics (metric_identifier, metric_date, unique_visitors, " +
                "total_sessions, total_pageviews, new_visitors, returning_visitors, bounce_rate, " +
                "avg_session_duration, pages_per_session, conversion_rate, goal_completions, " +
                "organic_traffic, paid_traffic, social_traffic, direct_traffic, avg_page_load_time, " +
                "error_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (metric_identifier) DO UPDATE SET " +
                "unique_visitors = EXCLUDED.unique_visitors, total_sessions = EXCLUDED.total_sessions, " +
                "total_pageviews = EXCLUDED. total_pageviews, new_visitors = EXCLUDED.new_visitors, " +
                "returning_visitors = EXCLUDED.returning_visitors, bounce_rate = EXCLUDED. bounce_rate, " +
                "avg_session_duration = EXCLUDED.avg_session_duration, pages_per_session = EXCLUDED.pages_per_session, " +
                "conversion_rate = EXCLUDED.conversion_rate, goal_completions = EXCLUDED.goal_completions, " +
                "organic_traffic = EXCLUDED. organic_traffic, paid_traffic = EXCLUDED.paid_traffic, " +
                "social_traffic = EXCLUDED.social_traffic, direct_traffic = EXCLUDED.direct_traffic, " +
                "avg_page_load_time = EXCLUDED.avg_page_load_time, error_count = EXCLUDED.error_count, " +
                "updated_at = CURRENT_TIMESTAMP";

        jdbcTemplate.update(sql, metric.getMetricIdentifier(), metric.getMetricDate(),
                metric.getUniqueVisitors(), metric.getTotalSessions(), metric.getTotalPageviews(),
                metric.getNewVisitors(), metric.getReturningVisitors(), metric.getBounceRate(),
                metric.getAvgSessionDuration(), metric.getPagesPerSession(), metric.getConversionRate(),
                metric.getGoalCompletions(), metric.getOrganicTraffic(), metric.getPaidTraffic(),
                metric.getSocialTraffic(), metric.getDirectTraffic(), metric.getAvgPageLoadTime(),
                metric.getErrorCount());

        return findByMetricIdentifier(metric. getMetricIdentifier()).orElse(metric);
    }

    public Optional<AnalyticsMetric> findByMetricIdentifier(String metricIdentifier) {
        String sql = "SELECT * FROM analytics_metrics WHERE metric_identifier = ?";
        List<AnalyticsMetric> results = jdbcTemplate.query(sql, rowMapper, metricIdentifier);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<AnalyticsMetric> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM analytics_metrics WHERE metric_date BETWEEN ?  AND ? ORDER BY metric_date";
        return jdbcTemplate.query(sql, rowMapper, startDate, endDate);
    }

    public Optional<AnalyticsMetric> findByDate(LocalDate date) {
        String sql = "SELECT * FROM analytics_metrics WHERE metric_date = ? ";
        List<AnalyticsMetric> results = jdbcTemplate.query(sql, rowMapper, date);
        return results. isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
