import axios from "axios";
import process from "process";

const api = axios.create({ 
    baseURL: 'http://localhost:8080'
});

export type Gym = {
    countryCode: string,
    countryName: string,
    cityShortName: string,
    cityName: string,
    createdAt: Date,
    shortName: string,
    displayName: string,
    websiteUrl: string | null,
    location: {
        latitude: number,
        longitude: number
    },
    streetAddress: string
};

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