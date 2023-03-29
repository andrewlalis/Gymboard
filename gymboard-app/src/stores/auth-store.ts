/**
 * This store keeps track of the authentication state of the web app, which
 * is just keeping the current user and their token.
 *
 * See src/api/main/auth.ts for mutators of this store.
 */

import { defineStore } from 'pinia';
import {TokenCredentials, User} from 'src/api/main/auth';
import api from 'src/api/main';
import {AxiosRequestConfig} from 'axios';
import Timeout = NodeJS.Timeout;

interface AuthState {
  /**
   * The currently authenticated user.
   */
  user: User | null;

  /**
   * The token that was used to authenticate the current user.
   */
  token: string | null;

  tokenRefreshTimer: Timeout | null;

  /**
   * The list of roles that the currently authenticated user has.
   */
  roles: string[];
}

const TOKEN_REFRESH_INTERVAL = 60 * 1000 * 25;

export const useAuthStore = defineStore('authStore', {
  state: (): AuthState => {
    return { user: null, token: null, tokenRefreshTimer: null, roles: [] };
  },
  getters: {
    /**
     * Determines whether a user is currently logged in.
     * @param state The store's state.
     */
    loggedIn: (state) => state.user !== null && state.token !== null,
    /**
     * Gets the axios config that can be applied to requests to authenticate
     * them as the current user. This will always return a valid request config
     * even when the user is not logged in.
     * @param state The store's state.
     */
    axiosConfig(state): AxiosRequestConfig {
      if (this.token !== null) {
        return {
          headers: { Authorization: 'Bearer ' + state.token },
        };
      } else {
        return {};
      }
    },
    /**
     * Getter that returns true if a user is authenticated, and the user is
     * an admin, meaning they have special access to additional data.
     * @param state The store's state.
     */
    isAdmin: state => state.roles.indexOf('admin') !== -1,
  },
  actions: {
    /**
     * Attempts to log in with the given token credentials.
     * @param credentials The credentials to use.
     */
    async logInWithCredentials(credentials: TokenCredentials) {
      const token = await api.auth.getNewToken(credentials);
      await this._logInWithToken(token);
    },
    /**
     * Attempts to log in with a token that's stored in the browser's local
     * storage.
     */
    async tryLogInWithStoredToken() {
      const token = localStorage.getItem('auth-token');
      if (token) {
        this.token = token; // Temporarily set our token to the one that was stored, so we can use it to request a new one.
        try {
          await this.refreshToken();
          if (this.token) { // If we were able to refresh the token, we can now go through with the login.
            await this._logInWithToken(token);
          }
        } catch (error) {
          console.warn('Could not log in with stored token: ', error);
          this.logOut();
        }
      }
    },
    /**
     * Initializes the auth state using a freshly-obtained token. This will
     * populate all the necessary data to consider the user to be logged in,
     * including fetching user data.
     *
     * Note: This method is intended to be used only internally.
     * @param token The token to use.
     */
    async _logInWithToken(token: string) {
      this.updateToken(token);
      this.user = await api.auth.getMyUser(this);
      const [personalDetails, preferences, roles] = await Promise.all([
        api.auth.getMyPersonalDetails(this),
        api.auth.getMyPreferences(this),
        api.auth.getMyRoles(this)
      ]);
      this.user.personalDetails = personalDetails;
      this.user.preferences = preferences;
      this.roles = roles;
      if (this.tokenRefreshTimer) {
        clearInterval(this.tokenRefreshTimer);
      }
      this.tokenRefreshTimer = setInterval(
        () => this.refreshToken(),
        TOKEN_REFRESH_INTERVAL
      );
    },
    /**
     * Refreshes the existing token used for authentication. If this fails,
     * the auth state will reset to the nominal logged-out state.
     */
    async refreshToken() {
      try {
        const newToken = await api.auth.refreshToken(this);
        this.updateToken(newToken);
      } catch (error) {
        console.error('Failed to refresh token: ', error);
        this.logOut();
      }
    },
    /**
     * Logs a user out of the application, resetting the auth state.
     */
    logOut() {
      this.user = null;
      this.token = null;
      if (this.tokenRefreshTimer) {
        clearInterval(this.tokenRefreshTimer);
      }
      this.tokenRefreshTimer = null;
      this.roles = [];
      localStorage.removeItem('auth-token');
    },
    /**
     * Updates the token that's stored for the currently authenticated user.
     * @param token The new token.
     */
    updateToken(token: string) {
      this.token = token;
      localStorage.setItem('auth-token', this.token);
    }
  }
});

export type AuthStoreType = ReturnType<typeof useAuthStore>;
