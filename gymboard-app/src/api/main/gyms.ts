import { GeoPoint } from 'src/api/main/models';
import SubmissionsModule, { ExerciseSubmission } from 'src/api/main/submission';
import { api } from 'src/api/main/index';
import { GymRoutable } from 'src/router/gym-routing';

export interface Gym {
  countryCode: string;
  countryName: string;
  cityShortName: string;
  cityName: string;
  createdAt: Date;
  shortName: string;
  displayName: string;
  websiteUrl: string | null;
  location: GeoPoint;
  streetAddress: string;
}

export interface SimpleGym {
  countryCode: string;
  cityShortName: string;
  shortName: string;
  displayName: string;
}

class GymsModule {
  public readonly submissions: SubmissionsModule = new SubmissionsModule();

  public async getGym(
    countryCode: string,
    cityShortName: string,
    gymShortName: string
  ) {
    const response = await api.get(
      `/gyms/${countryCode}_${cityShortName}_${gymShortName}`
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

  public async getRecentSubmissions(
    gym: GymRoutable
  ): Promise<Array<ExerciseSubmission>> {
    const response = await api.get(
      `/gyms/${gym.countryCode}_${gym.cityShortName}_${gym.shortName}/recent-submissions`
    );
    return response.data;
  }
}

export default GymsModule;
