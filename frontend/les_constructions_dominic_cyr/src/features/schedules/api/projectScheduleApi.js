import axiosInstance from '../../../utils/axios';

export const projectScheduleApi = {
  getProjectSchedules: async projectIdentifier => {
    const response = await axiosInstance.get(
      `/projects/${projectIdentifier}/schedules`
    );
    return response.data;
  },
};

export default projectScheduleApi;
