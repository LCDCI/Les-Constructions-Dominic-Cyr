import axiosInstance from '../../../utils/axios';

export const customerScheduleApi = {
  getCurrentWeekSchedules: async () => {
    const response = await axiosInstance.get('/customers/schedules');
    return response.data;
  },

  getAllSchedules: async () => {
    const response = await axiosInstance.get('/customers/schedules/all');
    return response.data;
  },

  getScheduleByIdentifier: async scheduleIdentifier => {
    const response = await axiosInstance.get(
      `/owners/schedules/${scheduleIdentifier}`
    );
    return response.data;
  },
};

export default customerScheduleApi;
