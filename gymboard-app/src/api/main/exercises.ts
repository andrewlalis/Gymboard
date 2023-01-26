import { api } from 'src/api/main/index';

export interface Exercise {
  shortName: string;
  displayName: string;
}

class ExercisesModule {
  public async getExercises(): Promise<Array<Exercise>> {
    const response = await api.get('/exercises');
    return response.data;
  }
}

export default ExercisesModule;
