package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class ScheduleIdentifier {

    @Column(name = "schedule_identifier", unique = true)
    private String scheduleId;

    public ScheduleIdentifier() {
        this.scheduleId = java.util.UUID.randomUUID().toString();
    }
    public ScheduleIdentifier(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    @Override
    public String toString() {
        return "ScheduleIdentifier{" +
                "scheduleId='" + (scheduleId == null ? "" : scheduleId) + '\'' +
                '}';
    }
}
