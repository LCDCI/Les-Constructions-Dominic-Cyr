import axiosInstance from '../../../utils/axios';

export const scheduleApi = {
  getSchedulesByProject: async (projectIdentifier, token = null) => {
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

  getScheduleById: async (scheduleIdentifier, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await axiosInstance.get(
      `/schedules/${scheduleIdentifier}`,
      { headers }
    );
    return response.data;
  },
};

export default scheduleApi;

