import { api } from 'src/api/main/index';

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
  public async getToken(credentials: TokenCredentials): Promise<string> {
    const response = await api.post('/auth/token', credentials);
    return response.data.token;
  }
  public async getMyUser(token: string): Promise<User> {
    const response = await api.get('/auth/me', {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });
    return response.data;
  }
}

export default AuthModule;
