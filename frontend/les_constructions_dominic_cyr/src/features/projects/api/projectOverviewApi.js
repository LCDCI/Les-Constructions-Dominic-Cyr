const API_BASE_URL =
  import.meta.env.VITE_API_BASE || '/api/v1';

const NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

export const projectOverviewApi = {
  getProjectOverview: async projectIdentifier => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}/overview`
    );
    if (!response.ok) {
      throw new Error('Failed to fetch project overview');
    }
    return response.json();
  },

  geocodeAddress: async address => {
    if (!address) return null;

    const params = new URLSearchParams({
      q: address,
      format: 'json',
      limit: 1,
    });

    const response = await fetch(`${NOMINATIM_URL}?${params.toString()}`);

    if (!response.ok) {
      return null;
    }

    const data = await response.json();

    if (data && data.length > 0) {
      const { lat, lon } = data[0];
      return [parseFloat(lat), parseFloat(lon)];
    }

    return null;
  },
};
