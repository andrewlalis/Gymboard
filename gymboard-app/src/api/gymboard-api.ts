import axios from 'axios';

export const BASE_URL = 'http://localhost:8080';

// TODO: Figure out how to get the base URL from environment.
const api = axios.create({ 
    baseURL: BASE_URL
});

export interface Exercise {
    shortName: string,
    displayName: string
};

export interface GeoPoint {
    latitude: number,
    longitude: number
};

export interface ExerciseSubmissionPayload {
    name: string,
    exerciseShortName: string,
    weight: number,
    videoId: number
};

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
};

export function getUploadUrl(gym: Gym) {
    return BASE_URL + `/gyms/${gym.countryCode}/${gym.cityShortName}/${gym.shortName}/submissions/upload`;
}

export function getFileUrl(fileId: number) {
    return BASE_URL + `/files/${fileId}`;
}

export async function getGym(countryCode: string, cityShortName: string, gymShortName: string): Promise<Gym> {
    const response = await api.get(`/gyms/${countryCode}/${cityShortName}/${gymShortName}`);
    const d = response.data;
    const gym: Gym = {
        countryCode: d.countryCode,
        countryName: d.countryName,
        cityShortName: d.cityShortName,
        cityName: d.cityName,
        createdAt: new Date(d.createdAt),
        shortName: d.shortName,
        displayName: d.displayName,
        websiteUrl: d.websiteUrl,
        location: d.location,
        streetAddress: d.streetAddress
    };
    return gym;
}

export async function getExercises(): Promise<Array<Exercise>> {
    const response = await api.get(`/exercises`);
    return response.data;
}
