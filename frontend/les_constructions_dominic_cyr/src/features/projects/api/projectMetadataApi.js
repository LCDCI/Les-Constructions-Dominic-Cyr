import apiClient from '../../../api/apiClient';

export const getProjectMetadata = async (projectIdentifier, token = null) => {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await apiClient.get(
    `/projects/${projectIdentifier}`,
    {
      headers,
    }
  );
  return response.data;
};
