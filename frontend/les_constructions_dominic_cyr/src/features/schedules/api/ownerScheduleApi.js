import axiosInstance from '../../../utils/axios';

export const ownerScheduleApi = {
  getCurrentWeekSchedules: async () => {
    const response = await axiosInstance.get('/owners/schedules');
    return response.data;
  },

  getAllSchedules: async () => {
    const response = await axiosInstance.get('/owners/schedules/all');
    return response.data;
  },

  getScheduleByIdentifier: async scheduleIdentifier => {
    const response = await axiosInstance.get(
      `/owners/schedules/${scheduleIdentifier}`
    );
    return response.data;
  },
};

export default ownerScheduleApi;
