import {RouteRecordRaw} from 'vue-router';
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
import AdminPage from 'pages/admin/AdminPage.vue';
import {useAuthStore} from 'stores/auth-store';
import AboutPage from 'pages/AboutPage.vue';
import UpdateEmailPage from "pages/auth/UpdateEmailPage.vue";
import RequestAccountDataPage from "pages/auth/RequestAccountDataPage.vue";
import DeleteAccountPage from "pages/auth/DeleteAccountPage.vue";

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
      { // The admin page, and all child pages within it, are only accessible for users with the 'admin' role.
        path: 'admin', component: AdminPage, beforeEnter: () => {
          const s = useAuthStore();
          if (!s.isAdmin) return '/'; // Redirect non-admins to the main page.
        }
      },
      { path: 'about', component: AboutPage },

      // Pages under /me are accessible only when authenticated.
      {
        path: 'me',
        beforeEnter: () => {
          const s = useAuthStore();
          if (!s.loggedIn) return '/';
        },
        children: [
          { path: 'settings', component: UserSettingsPage },
          { path: 'update-email', component: UpdateEmailPage },
          { path: 'request-account-data', component: RequestAccountDataPage },
          { path: 'delete-account', component: DeleteAccountPage }
        ]
      }
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
