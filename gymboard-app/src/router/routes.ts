import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'layouts/MainLayout.vue';
import GymSearchPage from 'pages/GymSearchPage.vue';
import GymPage from 'pages/gym/GymPage.vue';
import GymSubmissionPage from 'pages/gym/GymSubmissionPage.vue';
import GymHomePage from 'pages/gym/GymHomePage.vue';
import GymLeaderboardsPage from 'pages/gym/GymLeaderboardsPage.vue';
import LoginPage from 'pages/auth/LoginPage.vue';
import RegisterPage from 'pages/auth/RegisterPage.vue';
import RegistrationSuccessPage from 'pages/auth/RegistrationSuccessPage.vue';
import ActivationPage from 'pages/auth/ActivationPage.vue';
import SubmissionPage from 'pages/SubmissionPage.vue';
import UserPage from 'pages/user/UserPage.vue';
import UserSettingsPage from 'pages/auth/UserSettingsPage.vue';
import UserSearchPage from 'pages/UserSearchPage.vue';

const routes: RouteRecordRaw[] = [
  // Auth-related pages, which live outside the main layout.
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  { path: '/register/success', component: RegistrationSuccessPage },
  { path: '/activate', component: ActivationPage },

  // Main app:
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: GymSearchPage },
      { path: 'users', component: UserSearchPage },
      { path: 'users/:userId/settings', component: UserSettingsPage },
      { // Match anything under /users/:userId to the UserPage, since it manages sub-pages manually.
        path: 'users/:userId+',
        component: UserPage
      },
      {
        path: 'gyms/:countryCode/:cityShortName/:gymShortName',
        component: GymPage,
        children: [
          { path: '', component: GymHomePage },
          { path: 'submit', component: GymSubmissionPage },
          { path: 'leaderboard', component: GymLeaderboardsPage },
        ],
      },
      { path: 'submissions/:submissionId', component: SubmissionPage },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
