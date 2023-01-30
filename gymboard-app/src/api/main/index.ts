import axios from 'axios';
import GymsModule from 'src/api/main/gyms';
import ExercisesModule from 'src/api/main/exercises';
import LeaderboardsModule from 'src/api/main/leaderboards';
import AuthModule from 'src/api/main/auth';

export const BASE_URL = 'http://localhost:8080';

// TODO: Figure out how to get the base URL from environment.
export const api = axios.create({
  baseURL: BASE_URL,
});

class GymboardApi {
  public readonly auth = new AuthModule();
  public readonly gyms = new GymsModule();
  public readonly exercises = new ExercisesModule();
  public readonly leaderboards = new LeaderboardsModule();
}
export default new GymboardApi();
