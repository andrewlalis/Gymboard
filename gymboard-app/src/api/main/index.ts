import axios from 'axios';
import GymsModule from 'src/api/main/gyms';
import ExercisesModule from 'src/api/main/exercises';
import LeaderboardsModule from 'src/api/main/leaderboards';
import AuthModule from 'src/api/main/auth';
console.log(process.env);
export const api = axios.create({
  baseURL: process.env.API_URL,
});

class GymboardApi {
  public readonly auth = new AuthModule();
  public readonly gyms = new GymsModule();
  public readonly exercises = new ExercisesModule();
  public readonly leaderboards = new LeaderboardsModule();
}
export default new GymboardApi();
