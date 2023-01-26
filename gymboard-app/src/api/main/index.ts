import axios, { AxiosInstance } from 'axios';
import GymsModule from 'src/api/main/gyms';
import ExercisesModule from 'src/api/main/exercises';
import { GymRoutable } from 'src/router/gym-routing';

export const BASE_URL = 'http://localhost:8080';

// TODO: Figure out how to get the base URL from environment.
export const api = axios.create({
  baseURL: BASE_URL,
});

/**
 * The base class for all API modules.
 */
export abstract class ApiModule {
  protected api: AxiosInstance;

  protected constructor(api: AxiosInstance) {
    this.api = api;
  }
}

class GymboardApi {
  public readonly gyms = new GymsModule();
  public readonly exercises = new ExercisesModule();

  /**
   * Gets the URL for uploading a video file when creating an exercise submission
   * for a gym.
   * @param gym The gym that the submission is for.
   */
  public getUploadUrl(gym: GymRoutable) {
    return (
      BASE_URL +
      `/gyms/${gym.countryCode}_${gym.cityShortName}_${gym.shortName}/submissions/upload`
    );
  }

  /**
   * Gets the URL at which the raw file data for the given file id can be streamed.
   * @param fileId The file id.
   */
  public getFileUrl(fileId: number) {
    return BASE_URL + `/files/${fileId}`;
  }
}
export default new GymboardApi();
