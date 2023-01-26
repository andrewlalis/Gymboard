import axios from 'axios';
import { GymSearchResult } from 'src/api/search/models';

const api = axios.create({
  baseURL: 'http://localhost:8081',
});

/**
 * Searches for gyms using the given query, and eventually returns results.
 * @param query The query to use.
 */
export async function searchGyms(
  query: string
): Promise<Array<GymSearchResult>> {
  const response = await api.get('/search/gyms?q=' + query);
  return response.data;
}
