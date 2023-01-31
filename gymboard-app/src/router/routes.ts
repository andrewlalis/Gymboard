import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'layouts/MainLayout.vue';
import IndexPage from 'pages/IndexPage.vue';
import AboutPage from 'pages/AboutPage.vue';
import GymPage from 'pages/gym/GymPage.vue';
import GymSubmissionPage from 'pages/gym/GymSubmissionPage.vue';
import GymHomePage from 'pages/gym/GymHomePage.vue';
import GymLeaderboardsPage from 'pages/gym/GymLeaderboardsPage.vue';
import TestingPage from 'pages/TestingPage.vue';
import LoginPage from 'pages/auth/LoginPage.vue';
import RegisterPage from "pages/auth/RegisterPage.vue";
import RegistrationSuccessPage from "pages/auth/RegistrationSuccessPage.vue";
import ActivationPage from "pages/auth/ActivationPage.vue";

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
      { path: '', component: IndexPage },
      { path: 'testing', component: TestingPage },
      {
        path: 'gyms/:countryCode/:cityShortName/:gymShortName',
        component: GymPage,
        children: [
          { path: '', component: GymHomePage },
          { path: 'submit', component: GymSubmissionPage },
          { path: 'leaderboard', component: GymLeaderboardsPage },
        ],
      },
      { path: 'about', component: AboutPage }
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
