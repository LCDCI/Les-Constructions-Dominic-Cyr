import axiosInstance from '../../../utils/axios';

export const scheduleApi = {
  getCurrentWeekSchedules: async () => {
    try {
      const response = await axiosInstance.get('/salesperson/schedules');
      return response.data;
    } catch (error) {
      console.error('Error fetching current week schedules:', error);
      throw error;
    }
  },

  getAllSchedules: async () => {
    try {
      const response = await axiosInstance.get('/salesperson/schedules/all');
      return response.data;
    } catch (error) {
      console.error('Error fetching all schedules:', error);
      throw error;
    }
  },

  getScheduleByIdentifier: async (scheduleIdentifier) => {
    try {
      const response = await axiosInstance.get(`/salesperson/schedules/${scheduleIdentifier}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching schedule ${scheduleIdentifier}:`, error);
      throw error;
    }
  },
};

export default scheduleApi;