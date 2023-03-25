<template>
  <q-page>
    <StandardCenteredPage v-if="profile">
      <h3>{{ profile.name }}</h3>
      <div v-if="devStore.showDebugInfo">
        <div>Private: {{ profile.accountPrivate }}</div>
      </div>

      <div>
        <div>Followers: {{ profile.followerCount }}</div>
        <div>Following: {{ profile.followingCount }}</div>
      </div>

      <div v-if="authStore.loggedIn && !isOwnUser">
        <q-btn v-if="!profile.followingThisUser" label="Follow" @click="followUser"/>
        <q-btn v-if="profile.followingThisUser" label="Unfollow" @click="unfollowUser"/>
      </div>

      <PageMenu
        :base-route="`/users/${profile.id}`"
        :items="[
          {label: 'Lifts', to: ''},
          {label: 'Followers', to: 'followers'},
          {label: 'Following', to: 'following'}
        ]"
      />

      <!-- Sub-pages are rendered here. -->
      <div v-if="profile.canAccessThisUser">
        <UserSubmissionsPage :userId="profile.id" v-if="route.path === `/users/${profile.id}`"/>
        <UserFollowersPage :userId="profile.id" v-if="route.path === `/users/${profile.id}/followers`"/>
        <UserFollowingPage :userId="profile.id" v-if="route.path === `/users/${profile.id}/following`"/>
      </div>

      <!-- If the user can't be accessed, show a placeholder message instead. -->
      <div v-if="!profile.canAccessThisUser">
        This account is private.
      </div>

    </StandardCenteredPage>

    <!-- If no user profile was loaded, we show a generic "User not found" message and no content. -->
    <StandardCenteredPage v-if="!profile">
      <h3>{{ $t('userPage.notFound.title') }}</h3>
      <p>{{ $t('userPage.notFound.description') }}</p>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {onMounted, ref, Ref, watch} from 'vue';
import {UserFollowResponse, UserProfile} from 'src/api/main/auth';
import api from 'src/api/main';
import {useRoute} from 'vue-router';
import {useAuthStore} from 'stores/auth-store';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {showApiErrorToast, showInfoToast} from 'src/utils';
import PageMenu from 'components/PageMenu.vue';
import UserSubmissionsPage from 'pages/user/UserSubmissionsPage.vue';
import UserFollowersPage from 'pages/user/UserFollowersPage.vue';
import UserFollowingPage from 'pages/user/UserFollowingPage.vue';
import {useDevStore} from 'stores/dev-store';

const route = useRoute();
const authStore = useAuthStore();
const devStore = useDevStore();
const i18n = useI18n();
const quasar = useQuasar();

const profile: Ref<UserProfile | undefined> = ref();
const isOwnUser = ref(false);

// If the user id changes, we have to manually reload the new user, since we
// will end up on the same route component, which means the router won't
// re-render.
watch(route, async (updatedRoute) => {
  const userId = updatedRoute.params.userId[0];
  if (!profile.value || (profile.value.id !== userId)) {
    await loadUser(userId);
  }
});

onMounted(async () => {
  const userId = route.params.userId[0] as string;
  await loadUser(userId);
});

async function loadUser(id: string) {
  try {
    profile.value = await api.auth.getUserProfile(id, authStore);
    isOwnUser.value = authStore.loggedIn && profile.value.id === authStore.user?.id;
  } catch (error: any) {
    if (!(error.response && error.response.status === 404)) {
      showApiErrorToast(i18n, quasar);
    }
  }
}

async function followUser() {
  if (profile.value && !profile.value.followingThisUser) {
    try {
      const result = await api.auth.followUser(profile.value.id, authStore);
      if (result === UserFollowResponse.FOLLOWED) {
        await loadUser(profile.value.id);
      } else if (result === UserFollowResponse.REQUESTED) {
        showInfoToast(quasar, 'Requested to follow this user!');
      }
    } catch (error) {
      showApiErrorToast(i18n, quasar);
    }
  }
}

async function unfollowUser() {
  if (profile.value && profile.value.followingThisUser) {
    try {
      await api.auth.unfollowUser(profile.value.id, authStore);
      await loadUser(profile.value.id);
    } catch (error) {
      showApiErrorToast(i18n, quasar);
    }
  }
}
</script>

<style scoped>

</style>
