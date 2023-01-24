/**
 * Module for interacting with the Gymboard search service's API.
 */

import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081'
});

export interface GymSearchResult {
  shortName: string,
  displayName: string,
  cityShortName: string,
  cityName: string,
  countryCode: string,
  countryName: string,
  streetAddress: string,
  latitude: number,
  longitude: number
}

/**
 * Searches for gyms using the given query, and eventually returns results.
 * @param query The query to use.
 */
export async function searchGyms(query: string): Promise<Array<GymSearchResult>> {
  const response = await api.get('/search/gyms?q=' + query);
  return response.data;
}
