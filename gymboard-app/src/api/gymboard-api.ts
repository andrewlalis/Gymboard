import axios from 'axios';

export const BASE_URL = 'http://localhost:8080';

// TODO: Figure out how to get the base URL from environment.
const api = axios.create({
    baseURL: BASE_URL
});
api.defaults.headers.post['Content-Type'] = 'application/json';

export interface Exercise {
    shortName: string,
    displayName: string
}

export interface GeoPoint {
    latitude: number,
    longitude: number
}

export interface ExerciseSubmissionPayload {
    name: string,
    exerciseShortName: string,
    weight: number,
    reps: number,
    videoId: number
}

export interface ExerciseSubmission {
  id: number,
  createdAt: string,
  gym: SimpleGym,
  exercise: Exercise,
  status: string,
  submitterName: string,
  weight: number,
  reps: number
}

export interface Gym {
    countryCode: string,
    countryName: string,
    cityShortName: string,
    cityName: string,
    createdAt: Date,
    shortName: string,
    displayName: string,
    websiteUrl: string | null,
    location: GeoPoint,
    streetAddress: string
}

export interface SimpleGym {
  countryCode: string,
  cityShortName: string,
  shortName: string,
  displayName: string
}

/**
 * Gets the URL for uploading a video file when creating an exercise submission
 * for a gym.
 * @param gym The gym that the submission is for.
 */
export function getUploadUrl(gym: Gym) {
    return BASE_URL + `/gyms/${gym.countryCode}/${gym.cityShortName}/${gym.shortName}/submissions/upload`;
}

/**
 * Gets the URL at which the raw file data for the given file id can be streamed.
 * @param fileId The file id.
 */
export function getFileUrl(fileId: number) {
    return BASE_URL + `/files/${fileId}`;
}

export async function getExercises(): Promise<Array<Exercise>> {
    const response = await api.get('/exercises');
    return response.data;
}

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

export async function createSubmission(gym: Gym, payload: ExerciseSubmissionPayload): Promise<ExerciseSubmission> {
  const response = await api.post(
    `/gyms/${gym.countryCode}/${gym.cityShortName}/${gym.shortName}/submissions`,
    payload
  );
  return response.data;
}
