package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.UpcomingWork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpcomingWorkTest {

    @Test
    void testUpcomingWorkDefaultConstructor() {
        UpcomingWork upcomingWork = new UpcomingWork();
        
        assertNotNull(upcomingWork);
        assertNull(upcomingWork.getTasks());
    }

    @Test
    void testUpcomingWorkGetTasks() {
        UpcomingWork upcomingWork = new UpcomingWork();
        
        assertNull(upcomingWork.getTasks());
    }

    @Test
    void testUpcomingWorkIsEmbeddable() {
        // Test that UpcomingWork can be instantiated as an embeddable
        UpcomingWork upcomingWork = new UpcomingWork();
        
        assertNotNull(upcomingWork);
        // The class should have a no-arg constructor and a getter for tasks
        assertNotNull(upcomingWork.getClass().getAnnotation(jakarta.persistence.Embeddable.class));
    }
}
