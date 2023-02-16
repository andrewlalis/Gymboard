import {api} from 'src/api/main';
import {AuthStoreType} from 'stores/auth-store';
import {ExerciseSubmission, parseSubmission} from 'src/api/main/submission';

class UsersModule {
  public async getRecentSubmissions(userId: string, authStore: AuthStoreType): Promise<Array<ExerciseSubmission>> {
    const response = await api.get(`/users/${userId}/recent-submissions`, authStore.axiosConfig);
    return response.data.map(parseSubmission);
  }
}

export default UsersModule;
