/**
 * This store keeps track of the authentication state of the web app, which
 * is just keeping the current user and their token.
 *
 * See src/api/main/auth.ts for mutators of this store.
 */

import { defineStore } from 'pinia';
import { User } from 'src/api/main/auth';

interface AuthState {
  user: User | null;
  token: string | null;
}

export const useAuthStore = defineStore('authStore', {
  state: (): AuthState => {
    return { user: null, token: null };
  },
  getters: {
    loggedIn: (state) => state.user !== null && state.token !== null,
    axiosConfig(state) {
      if (this.token !== null) {
        return {
          headers: { Authorization: 'Bearer ' + state.token },
        };
      } else {
        return {};
      }
    },
  },
});

export type AuthStoreType = ReturnType<typeof useAuthStore>;
