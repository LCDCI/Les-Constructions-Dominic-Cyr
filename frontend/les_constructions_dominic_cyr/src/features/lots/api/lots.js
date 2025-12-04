import client from '../../../client';

export async function fetchLots() {
  const res = await client.get('/lots');
  return res.data || [];
}

export async function fetchLotById(id) {
  const res = await client.get(`/lots/${id}`);
  return res.data;
}
