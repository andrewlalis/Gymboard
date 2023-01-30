import { api } from 'src/api/main/index';
import { AuthStoreType } from 'stores/auth-store';
import Timeout = NodeJS.Timeout;

export interface User {
  id: string;
  activated: boolean;
  email: string;
  name: string;
}

export interface TokenCredentials {
  email: string;
  password: string;
}

class AuthModule {
  private static readonly TOKEN_REFRESH_INTERVAL_MS = 30000;

  private tokenRefreshTimer?: Timeout;

  public async login(authStore: AuthStoreType, credentials: TokenCredentials) {
    authStore.token = await this.fetchNewToken(credentials);
    authStore.user = await this.fetchMyUser(authStore);

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

  private async fetchNewToken(credentials: TokenCredentials): Promise<string> {
    const response = await api.post('/auth/token', credentials);
    return response.data.token;
  }

  private async refreshToken(authStore: AuthStoreType) {
    const response = await api.get('/auth/token', authStore.axiosConfig);
    authStore.token = response.data.token;
  }

  private async fetchMyUser(authStore: AuthStoreType): Promise<User> {
    const response = await api.get('/auth/me', authStore.axiosConfig);
    return response.data;
  }
}

export default AuthModule;
