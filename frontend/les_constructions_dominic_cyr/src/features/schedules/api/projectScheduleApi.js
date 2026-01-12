import axiosInstance from '../../../utils/axios';

export const projectScheduleApi = {
  getProjectSchedules: async projectIdentifier => {
    const response = await axiosInstance.get(
      `/projects/${projectIdentifier}/schedules`
    );
    return response.data;
  },

  getProjectSchedule: async (projectIdentifier, scheduleIdentifier) => {
    const response = await axiosInstance.get(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`
    );
    return response.data;
  },

  createProjectSchedule: async (projectIdentifier, scheduleData) => {
    const response = await axiosInstance.post(
      `/projects/${projectIdentifier}/schedules`,
      scheduleData
    );
    return response.data;
  },

  updateProjectSchedule: async (projectIdentifier, scheduleIdentifier, scheduleData) => {
    const response = await axiosInstance.put(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`,
      scheduleData
    );
    return response.data;
  },

  deleteProjectSchedule: async (projectIdentifier, scheduleIdentifier) => {
    const response = await axiosInstance.delete(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`
    );
    return response.data;
  },
};

export default projectScheduleApi;
