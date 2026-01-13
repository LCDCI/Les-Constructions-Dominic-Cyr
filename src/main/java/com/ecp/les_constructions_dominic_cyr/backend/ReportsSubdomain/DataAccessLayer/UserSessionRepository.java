package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core. RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class UserSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserSession> rowMapper = (rs, rowNum) -> {
        UserSession session = new UserSession();
        session.setSessionId(rs.getLong("session_id"));
        session.setSessionIdentifier(rs.getString("session_identifier"));
        session.setUserId(rs.getString("user_id"));
        session.setVisitorId(rs.getString("visitor_id"));
        session.setSessionStart(rs.getTimestamp("session_start").toLocalDateTime());
        if (rs.getTimestamp("session_end") != null) {
            session.setSessionEnd(rs.getTimestamp("session_end").toLocalDateTime());
        }
        session. setDurationSeconds(rs.getInt("duration_seconds"));
        session.setPagesViewed(rs.getInt("pages_viewed"));
        session.setIsNewVisitor(rs.getBoolean("is_new_visitor"));
        session.setReferrerSource(rs.getString("referrer_source"));
        session.setDeviceType(rs.getString("device_type"));
        session.setBrowser(rs.getString("browser"));
        session.setLocation(rs.getString("location"));
        session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return session;
    };

    public UserSession save(UserSession session) {
        String sql = "INSERT INTO user_sessions (session_identifier, user_id, visitor_id, " +
                "session_start, session_end, duration_seconds, pages_viewed, is_new_visitor, " +
                "referrer_source, device_type, browser, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, session.getSessionIdentifier(), session.getUserId(),
                session.getVisitorId(), session.getSessionStart(), session.getSessionEnd(),
                session.getDurationSeconds(), session.getPagesViewed(), session.getIsNewVisitor(),
                session.getReferrerSource(), session.getDeviceType(), session.getBrowser(),
                session.getLocation());

        return session;
    }

    public List<UserSession> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM user_sessions WHERE DATE(session_start) BETWEEN ? AND ?  ORDER BY session_start";
        return jdbcTemplate.query(sql, rowMapper, startDate, endDate);
    }

    public int countUniqueVisitorsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(DISTINCT visitor_id) FROM user_sessions WHERE DATE(session_start) BETWEEN ? AND ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
        return count != null ? count : 0;
    }
}
