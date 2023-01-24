import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

export interface GymIdentifiable {
  countryCode: string;
  cityShortName: string;
  shortName: string;
}

export type Gym = {
  countryCode: string;
  countryName: string;
  cityShortName: string;
  cityName: string;
  createdAt: Date;
  shortName: string;
  displayName: string;
  websiteUrl: string | null;
  location: {
    latitude: number;
    longitude: number;
  };
  streetAddress: string;
};

export async function getGym(
  countryCode: string,
  cityShortName: string,
  gymShortName: string
): Promise<Gym> {
  const response = await api.get(
    `/gyms/${countryCode}/${cityShortName}/${gymShortName}`
  );
  const d = response.data;
  return {
    countryCode: d.countryCode,
    countryName: d.countryName,
    cityShortName: d.cityShortName,
    cityName: d.cityName,
    createdAt: new Date(d.createdAt),
    shortName: d.shortName,
    displayName: d.displayName,
    websiteUrl: d.websiteUrl,
    location: d.location,
    streetAddress: d.streetAddress,
  };
}

export function getGymRoute(gym: GymIdentifiable): string {
  return `/g/${gym.countryCode}/${gym.cityShortName}/${gym.shortName}`;
}
