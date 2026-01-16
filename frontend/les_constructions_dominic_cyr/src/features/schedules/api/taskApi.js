import axiosInstance from '../../../utils/axios';

export const taskApi = {
  createTask: async (taskData, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await axiosInstance.post('/owners/tasks', taskData, { headers });
    return response.data;
  },

  getTasksForSchedule: async (scheduleIdentifier, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await axiosInstance.get(
      `/schedules/${scheduleIdentifier}/tasks`,
      { headers }
    );
    return response.data;
  },

  getTaskById: async (taskId, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await axiosInstance.get(`/owners/tasks/${taskId}`, {
      headers,
    });
    return response.data;
  },

  updateTask: async (taskId, taskData, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await axiosInstance.put(
      `/owners/tasks/${taskId}`,
      taskData,
      { headers }
    );
    return response.data;
  },

  deleteTask: async (taskId, token = null) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await axiosInstance.delete(`/owners/tasks/${taskId}`, {
      headers,
    });
    return response.data;
  },
};

export default taskApi;
