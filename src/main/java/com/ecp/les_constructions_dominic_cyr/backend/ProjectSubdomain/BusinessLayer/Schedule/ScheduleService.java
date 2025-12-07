package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;

import java.util.List;

public interface ScheduleService {

    List<ScheduleResponseDTO> getCurrentWeekSchedules();

    List<ScheduleResponseDTO> getAllSchedules();

    ScheduleResponseDTO getScheduleByIdentifier(String scheduleIdentifier);
}