import apiClient from '../../../api/apiClient';

export const getProjectMetadata = async (projectIdentifier, userId) => {
  const response = await apiClient.get(
    `/projects/${projectIdentifier}/metadata`,
    {
      headers: {
        'X-User-Id': userId,
      },
    }
  );
  return response.data;
};
