package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class TaskIdentifier {

    @Column(name = "task_identifier", unique = true)
    private String taskId;

    public TaskIdentifier() {
        this.taskId = java.util.UUID.randomUUID().toString();
    }
    public TaskIdentifier(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "TaskIdentifier{" +
                "taskId='" + (taskId == null ? "" : taskId) + '\'' +
                '}';
    }
}
