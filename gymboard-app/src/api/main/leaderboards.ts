import { ExerciseSubmission, parseSubmission } from 'src/api/main/submission';
import { getGymCompoundId, GymRoutable } from 'src/router/gym-routing';
import { api } from 'src/api/main/index';

export enum LeaderboardTimeframe {
  DAY = 'DAY',
  WEEK = 'WEEK',
  MONTH = 'MONTH',
  YEAR = 'YEAR',
  ALL = 'ALL',
}

export interface LeaderboardParams {
  exerciseShortName?: string;
  gyms?: Array<GymRoutable>;
  timeframe?: LeaderboardTimeframe;
  page?: number;
  size?: number;
}

interface RequestParams {
  exercise?: string;
  gyms?: string;
  t?: string;
  page?: number;
  size?: number;
}

class LeaderboardsModule {
  public async getLeaderboard(
    params: LeaderboardParams
  ): Promise<Array<ExerciseSubmission>> {
    const requestParams: RequestParams = {};
    if (params.exerciseShortName) {
      requestParams.exercise = params.exerciseShortName;
    }
    if (params.gyms) {
      requestParams.gyms = params.gyms
        .map((gym) => getGymCompoundId(gym))
        .join(',');
    }
    if (params.timeframe) {
      requestParams.t = params.timeframe;
    }
    if (params.page) requestParams.page = params.page;
    if (params.size) requestParams.size = params.size;

    const response = await api.get('/leaderboards', { params: requestParams });
    return response.data.content.map(parseSubmission);
  }
}

export default LeaderboardsModule;
