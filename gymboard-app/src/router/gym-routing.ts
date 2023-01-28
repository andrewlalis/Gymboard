import { useRoute } from 'vue-router';
import { Gym } from 'src/api/main/gyms';
import api from 'src/api/main';

/**
 * Any object that contains the properties needed to identify a single gym.
 */
export interface GymRoutable {
  countryCode: string;
  cityShortName: string;
  shortName: string;
}

/**
 * Gets the route that can be used to navigate to a particular gym's page.
 * @param gym The gym to get the route for.
 */
export function getGymRoute(gym: GymRoutable): string {
  return `/gyms/${gym.countryCode}/${gym.cityShortName}/${gym.shortName}`;
}

/**
 * Gets the gym that's referred to by the current route's path params.
 */
export async function getGymFromRoute(): Promise<Gym> {
  const route = useRoute();
  return await api.gyms.getGym(
    route.params.countryCode as string,
    route.params.cityShortName as string,
    route.params.gymShortName as string
  );
}

/**
 * Gets the compound id for a gym; that is, the universally unique identifier
 * that can be used for certain API requests.
 * @param gym The gym to get the compound id for.
 */
export function getGymCompoundId(gym: GymRoutable): string {
  return `${gym.countryCode}_${gym.cityShortName}_${gym.shortName}`;
}