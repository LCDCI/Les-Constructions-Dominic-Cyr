import axiosInstance from '../../../utils/axios';

const buildHeaders = token => (token ? { Authorization: `Bearer ${token}` } : {});

const withFallback = async (primaryCall, fallbackCall) => {
  try {
    return await primaryCall();
  } catch (err) {
    if (typeof fallbackCall === 'function') {
      return fallbackCall();
    }
    throw err;
  }
};

export const taskApi = {
  createTask: async (taskData, token = null) => {
    const headers = buildHeaders(token);

    const response = await withFallback(
      () => axiosInstance.post('/tasks', taskData, { headers }),
      () => axiosInstance.post('/owners/tasks', taskData, { headers })
    );

    return response.data;
  },

  getTasksForSchedule: async (scheduleIdentifier, token = null) => {
    const headers = buildHeaders(token);

    const response = await axiosInstance.get(
      `/schedules/${scheduleIdentifier}/tasks`,
      { headers }
    );
    return response.data;
  },

  getTaskById: async (taskId, token = null) => {
    const headers = buildHeaders(token);

    const response = await withFallback(
      () => axiosInstance.get(`/tasks/${taskId}`, { headers }),
      () => axiosInstance.get(`/owners/tasks/${taskId}`, { headers })
    );

    return response.data;
  },

  updateTask: async (taskId, taskData, token = null) => {
    const headers = buildHeaders(token);

    const tryUpdate = async () => {
      try {
        return await axiosInstance.put(`/tasks/${taskId}`, taskData, {
          headers,
        });
      } catch (err) {
        // Final fallback to owner path (legacy)
        return await axiosInstance.put(`/owners/tasks/${taskId}`, taskData, {
          headers,
        });
      }
    };

    const response = await tryUpdate();
    return response.data;
  },

  deleteTask: async (taskId, token = null) => {
    const headers = buildHeaders(token);

    const response = await withFallback(
      () => axiosInstance.delete(`/tasks/${taskId}`, { headers }),
      () => axiosInstance.delete(`/owners/tasks/${taskId}`, { headers })
    );

    return response.data;
  },
};

export default taskApi;
