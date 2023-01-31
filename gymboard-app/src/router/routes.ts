import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'layouts/MainLayout.vue';
import IndexPage from 'pages/IndexPage.vue';
import GymPage from 'pages/gym/GymPage.vue';
import GymSubmissionPage from 'pages/gym/GymSubmissionPage.vue';
import GymHomePage from 'pages/gym/GymHomePage.vue';
import GymLeaderboardsPage from 'pages/gym/GymLeaderboardsPage.vue';
import TestingPage from 'pages/TestingPage.vue';
import LoginPage from 'pages/LoginPage.vue';

const routes: RouteRecordRaw[] = [
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
    ],
  },
  { path: '/login', component: LoginPage },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
