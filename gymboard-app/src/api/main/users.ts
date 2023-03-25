import {api} from 'src/api/main';
import {AuthStoreType} from 'stores/auth-store';
import {ExerciseSubmission, parseSubmission} from 'src/api/main/submission';
import {defaultPaginationOptions, Page, PaginationOptions, toQueryParams} from 'src/api/main/models';

class UsersModule {
  public async getRecentSubmissions(userId: string, authStore: AuthStoreType): Promise<Array<ExerciseSubmission>> {
    const response = await api.get(`/users/${userId}/recent-submissions`, authStore.axiosConfig);
    return response.data.map(parseSubmission);
  }

  public async getSubmissions(userId: string, authStore: AuthStoreType, paginationOptions: PaginationOptions = defaultPaginationOptions()): Promise<Page<ExerciseSubmission>> {
    const config = structuredClone(authStore.axiosConfig);
    config.params = toQueryParams(paginationOptions);
    const response = await api.get(`/users/${userId}/submissions`, {...toQueryParams(paginationOptions), ...authStore.axiosConfig});
    response.data.content = response.data.content.map(parseSubmission);
    return response.data;
  }
}

export default UsersModule;
