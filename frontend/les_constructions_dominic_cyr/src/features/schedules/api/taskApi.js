import axiosInstance from '../../../utils/axios';

const buildHeaders = token =>
  token ? { Authorization: `Bearer ${token}` } : {};

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
      async () => {
        try {
          return await axiosInstance.get(`/contractors/tasks/${taskId}`, {
            headers,
          });
        } catch (e) {
          return await axiosInstance.get(`/owners/tasks/${taskId}`, {
            headers,
          });
        }
      }
    );
    return response.data;
  },

  updateTask: async (taskId, taskData, token = null) => {
    const headers = buildHeaders(token);

    const tryUpdate = async () => {
      try {
        // 1. Try generic tasks endpoint
        return await axiosInstance.put(`/tasks/${taskId}`, taskData, {
          headers,
        });
      } catch (err) {
        // If 403 or 404, try the contractor-specific route we added to the backend
        try {
          return await axiosInstance.put(
            `/contractors/tasks/${taskId}`,
            taskData,
            { headers }
          );
        } catch (err2) {
          // 3. Final fallback to owner path (Legacy/Strict Owner)
          return await axiosInstance.put(`/owners/tasks/${taskId}`, taskData, {
            headers,
          });
        }
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

  // Contractor task view endpoints
  getAllTasksForContractorView: async (token = null) => {
    const headers = buildHeaders(token);
    const response = await axiosInstance.get('/contractors/tasks/all', {
      headers,
    });
    return response.data;
  },

  getAllTasksGroupedByProjectAndLot: async (token = null) => {
    const headers = buildHeaders(token);
    const response = await axiosInstance.get('/contractors/tasks/grouped', {
      headers,
    });
    return response.data;
  },

  getTasksForProjectGroupedByLot: async (projectIdentifier, token = null) => {
    const headers = buildHeaders(token);
    const response = await axiosInstance.get(
      `/contractors/projects/${projectIdentifier}/tasks`,
      { headers }
    );
    return response.data;
  },

  getTasksForLot: async (projectIdentifier, lotId, token = null) => {
    const headers = buildHeaders(token);
    const response = await axiosInstance.get(
      `/contractors/projects/${projectIdentifier}/lots/${lotId}/tasks`,
      { headers }
    );
    return response.data;
  },

  getContractorAssignedTasksWithDetails: async (contractorId, token = null) => {
    const headers = buildHeaders(token);
    const response = await axiosInstance.get(
      `/contractors/${contractorId}/assigned-tasks`,
      { headers }
    );
    return response.data;
  },
};

export default taskApi;
