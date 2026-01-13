package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java. util.List;
import java.util.Map;

@Repository
public class PageViewRepository {

    private final JdbcTemplate jdbcTemplate;

    public PageViewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findTopPagesByDateRange(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = "SELECT page_url, page_title, COUNT(*) as view_count, " +
                "AVG(time_on_page_seconds) as avg_time_on_page, " +
                "SUM(CASE WHEN is_exit = true THEN 1 ELSE 0 END) as exit_count " +
                "FROM page_views " +
                "WHERE DATE(view_timestamp) BETWEEN ? AND ? " +
                "GROUP BY page_url, page_title " +
                "ORDER BY view_count DESC " +
                "LIMIT ?";
        return jdbcTemplate.queryForList(sql, startDate, endDate, limit);
    }

    public List<Map<String, Object>> findPagePerformanceByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT page_url, COUNT(*) as total_views, " +
                "AVG(time_on_page_seconds) as avg_time, " +
                "SUM(CASE WHEN is_bounce = true THEN 1 ELSE 0 END) as bounce_count " +
                "FROM page_views " +
                "WHERE DATE(view_timestamp) BETWEEN ? AND ? " +
                "GROUP BY page_url " +
                "ORDER BY total_views DESC";
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public int countTotalPageviewsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM page_views WHERE DATE(view_timestamp) BETWEEN ? AND ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
        return count != null ?  count : 0;
    }
}
