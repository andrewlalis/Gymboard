/**
 * This store keeps track of the authentication state of the web app, which
 * is just keeping the current user and their token.
 *
 * See src/api/main/auth.ts for mutators of this store.
 */

import { defineStore } from 'pinia';
import { User } from 'src/api/main/auth';
import {AxiosRequestConfig} from 'axios';

interface AuthState {
  /**
   * The currently authenticated user.
   */
  user: User | null;

  /**
   * The token that was used to authenticate the current user.
   */
  token: string | null;

  /**
   * The list of roles that the currently authenticated user has.
   */
  roles: string[];
}

export const useAuthStore = defineStore('authStore', {
  state: (): AuthState => {
    return { user: null, token: null, roles: [] };
  },
  getters: {
    loggedIn: (state) => state.user !== null && state.token !== null,
    axiosConfig(state): AxiosRequestConfig {
      if (this.token !== null) {
        return {
          headers: { Authorization: 'Bearer ' + state.token },
        };
      } else {
        return {};
      }
    },
    isAdmin: state => state.roles.indexOf('admin') !== -1,
  },
});

export type AuthStoreType = ReturnType<typeof useAuthStore>;
