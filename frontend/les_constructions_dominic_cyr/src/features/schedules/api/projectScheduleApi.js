import axiosInstance from '../../../utils/axios';

export const projectScheduleApi = {
  getProjectSchedules: async (projectIdentifier, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.get(
      `/projects/${projectIdentifier}/schedules`,
      { headers }
    );
    return response.data;
  },

  getProjectSchedule: async (
    projectIdentifier,
    scheduleIdentifier,
    token = null
  ) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.get(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`,
      { headers }
    );
    return response.data;
  },

  createProjectSchedule: async (
    projectIdentifier,
    scheduleData,
    token = null
  ) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.post(
      `/projects/${projectIdentifier}/schedules`,
      scheduleData,
      { headers }
    );
    return response.data;
  },

  updateProjectSchedule: async (
    projectIdentifier,
    scheduleIdentifier,
    scheduleData,
    token = null
  ) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.put(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`,
      scheduleData,
      { headers }
    );
    return response.data;
  },

  deleteProjectSchedule: async (
    projectIdentifier,
    scheduleIdentifier,
    token = null
  ) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.delete(
      `/projects/${projectIdentifier}/schedules/${scheduleIdentifier}`,
      { headers }
    );
    return response.data;
  },
};

export default projectScheduleApi;
