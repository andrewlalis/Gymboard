import {api} from 'src/api/main/index';
import {AuthStoreType} from 'stores/auth-store';
import Timeout = NodeJS.Timeout;
import {WeightUnit} from 'src/api/main/submission';

export interface User {
  id: string;
  activated: boolean;
  email: string;
  name: string;
  personalDetails?: UserPersonalDetails;
  preferences?: UserPreferences;
}

export enum PersonSex {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  UNKNOWN = 'UNKNOWN'
}

export interface UserPersonalDetails {
  userId: string;
  birthDate?: string;
  currentWeight?: number;
  currentWeightUnit?: WeightUnit;
  currentMetricWeight?: number;
  sex: PersonSex;
}

export interface UserPreferences {
  userId: string;
  accountPrivate: boolean;
  locale: string;
}

export interface TokenCredentials {
  email: string;
  password: string;
}

export interface UserCreationPayload {
  name: string;
  email: string;
  password: string;
}

class AuthModule {
  private static readonly TOKEN_REFRESH_INTERVAL_MS = 30000;

  private tokenRefreshTimer?: Timeout;

  /**
   * Attempts to use the given credentials to obtain an access token for
   * sending authenticated requests.
   * @param authStore The auth store to use to update app state.
   * @param credentials The credentials for logging in.
   */
  public async login(authStore: AuthStoreType, credentials: TokenCredentials) {
    authStore.token = await this.getNewToken(credentials);
    authStore.user = await this.getMyUser(authStore);
    // Load the user's attached data right away too.
    authStore.user.personalDetails = await this.getMyPersonalDetails(authStore);
    authStore.user.preferences = await this.getMyPreferences(authStore);

    clearTimeout(this.tokenRefreshTimer);
    this.tokenRefreshTimer = setTimeout(
      () => this.refreshToken(authStore),
      AuthModule.TOKEN_REFRESH_INTERVAL_MS
    );
  }

  public logout(authStore: AuthStoreType) {
    authStore.$reset();
    clearTimeout(this.tokenRefreshTimer);
  }

  public async register(payload: UserCreationPayload): Promise<User> {
    const response = await api.post('/auth/register', payload);
    return response.data;
  }

  public async activateUser(code: string): Promise<User> {
    const response = await api.post('/auth/activate', { code: code });
    return response.data;
  }

  public async getNewToken(credentials: TokenCredentials): Promise<string> {
    const response = await api.post('/auth/token', credentials);
    return response.data.token;
  }

  public async refreshToken(authStore: AuthStoreType) {
    const response = await api.get('/auth/token', authStore.axiosConfig);
    authStore.token = response.data.token;
  }

  public async getMyUser(authStore: AuthStoreType): Promise<User> {
    const response = await api.get('/auth/me', authStore.axiosConfig);
    return response.data;
  }

  public async getUser(userId: string, authStore: AuthStoreType): Promise<User> {
    const response = await api.get(`/auth/users/${userId}`, authStore.axiosConfig);
    return response.data;
  }

  public async updatePassword(newPassword: string, authStore: AuthStoreType) {
    await api.post(
      '/auth/me/password',
      { newPassword: newPassword },
      authStore.axiosConfig
    );
  }

  public async generatePasswordResetCode(email: string) {
    await api.get('/auth/reset-password', { params: { email: email } });
  }

  public async resetPassword(resetCode: string, newPassword: string) {
    await api.post('/auth/reset-password', {
      code: resetCode,
      newPassword: newPassword,
    });
  }

  public async getMyPersonalDetails(authStore: AuthStoreType): Promise<UserPersonalDetails> {
    const response = await api.get('/auth/me/personal-details', authStore.axiosConfig);
    return response.data;
  }

  public async updateMyPersonalDetails(authStore: AuthStoreType, newPersonalDetails: UserPersonalDetails): Promise<UserPersonalDetails> {
    const response = await api.post('/auth/me/personal-details', newPersonalDetails, authStore.axiosConfig);
    return response.data;
  }

  public async getMyPreferences(authStore: AuthStoreType): Promise<UserPreferences> {
    const response = await api.get('/auth/me/preferences', authStore.axiosConfig);
    return response.data;
  }

  public async updateMyPreferences(authStore: AuthStoreType, newPreferences: UserPreferences): Promise<UserPreferences> {
    const response = await api.post('/auth/me/preferences', newPreferences, authStore.axiosConfig);
    return response.data;
  }
}

export default AuthModule;
