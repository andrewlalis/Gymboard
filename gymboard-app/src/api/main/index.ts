import axios from 'axios';
import GymsModule from 'src/api/main/gyms';
import ExercisesModule from 'src/api/main/exercises';
import LeaderboardsModule from 'src/api/main/leaderboards';
import AuthModule from 'src/api/main/auth';
import UsersModule from 'src/api/main/users';

export const api = axios.create({
  baseURL: process.env.API_URL,
});

/**
 * The main Gymboard API namespace, containing various modules that deal with
 * different parts of the API, like auth or users.
 */
class GymboardApi {
  public readonly auth = new AuthModule();
  public readonly gyms = new GymsModule();
  public readonly users = new UsersModule();
  public readonly exercises = new ExercisesModule();
  public readonly leaderboards = new LeaderboardsModule();

  /**
   * Gets the status of the Gymboard API.
   */
  public async getStatus(): Promise<boolean> {
    try {
      await api.get('/status');
      return true;
    } catch (e) {
      return false;
    }
  }
}
export default new GymboardApi();
