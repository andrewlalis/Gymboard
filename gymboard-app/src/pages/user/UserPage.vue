<template>
  <q-page>
    <StandardCenteredPage v-if="user">
      <h3>{{ user?.name }}</h3>

      <div v-if="relationship">
        <q-btn v-if="!relationship.following" label="Follow" @click="followUser"/>
        <q-btn v-if="relationship.following" label="Unfollow" @click="unfollowUser"/>
      </div>

      <PageMenu
        :base-route="`/users/${user.id}`"
        :items="[
          {label: 'Lifts', to: ''},
          {label: 'Followers', to: 'followers'},
          {label: 'Following', to: 'following'}
        ]"
      />

      <!-- Sub-pages are rendered here. -->
      <div v-if="userAccessible">
        <UserSubmissionsPage :user="user" v-if="route.path === getUserRoute(user)"/>
        <UserFollowersPage :user="user" v-if="route.path === getUserRoute(user) + '/followers'"/>
        <UserFollowingPage :user="user" v-if="route.path === getUserRoute(user) + '/following'"/>
      </div>

      <div v-if="!userAccessible">
        This account is private.
      </div>

    </StandardCenteredPage>
    <StandardCenteredPage v-if="userNotFound">
      <h3>{{ $t('userPage.notFound.title') }}</h3>
      <p>{{ $t('userPage.notFound.description') }}</p>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {onMounted, ref, Ref, watch} from 'vue';
import {User, UserRelationship} from 'src/api/main/auth';
import api from 'src/api/main';
import {useRoute} from 'vue-router';
import {useAuthStore} from 'stores/auth-store';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {showApiErrorToast} from 'src/utils';
import PageMenu from 'components/PageMenu.vue';
import UserSubmissionsPage from 'pages/user/UserSubmissionsPage.vue';
import {getUserRoute} from 'src/router/user-routing';
import UserFollowersPage from 'pages/user/UserFollowersPage.vue';
import UserFollowingPage from 'pages/user/UserFollowingPage.vue';

const route = useRoute();
const authStore = useAuthStore();
const i18n = useI18n();
const quasar = useQuasar();

const user: Ref<User | undefined> = ref();
const relationship: Ref<UserRelationship | undefined> = ref();
const isOwnUser = ref(false);
const userNotFound = ref(false);
const userAccessible = ref(false);

// If the user id changes, we have to manually reload the new user, since we
// will end up on the same route component, which means the router won't
// re-render.
watch(route, async (updatedRoute) => {
  const userId = updatedRoute.params.userId[0];
  if (!user.value || user.value.id !== userId) {
    await loadUser(userId);
  }
});

onMounted(async () => {
  const userId = route.params.userId[0] as string;
  await loadUser(userId);
});

async function loadUser(id: string) {
  try {
    user.value = await api.auth.getUser(id, authStore);
    isOwnUser.value = authStore.loggedIn && user.value.id === authStore.user?.id;
    userAccessible.value = await api.auth.isUserAccessible(id, authStore);
    await loadRelationship();
  } catch (error: any) {
    user.value = undefined;
    relationship.value = undefined;
    isOwnUser.value = false;
    userAccessible.value = false;
    if (error.response && error.response.status === 404) {
      userNotFound.value = true;
    } else {
      showApiErrorToast(i18n, quasar);
    }
  }
}

async function loadRelationship() {
  if (authStore.user && user.value && userAccessible.value && authStore.user.id !== user.value.id) {
    try {
      relationship.value = await api.auth.getRelationshipTo(authStore.user.id, user.value.id, authStore);
    } catch (error) {
      relationship.value = undefined;
      showApiErrorToast(i18n, quasar);
    }
  } else {
    relationship.value = undefined;
  }
}

async function followUser() {
  if (user.value) {
    try {
      await api.auth.followUser(user.value?.id, authStore);
      await loadRelationship();
    } catch (error) {
      showApiErrorToast(i18n, quasar);
    }
  }
}

async function unfollowUser() {
  if (user.value) {
    try {
      await api.auth.unfollowUser(user.value?.id, authStore);
      await loadRelationship();
    } catch (error) {
      showApiErrorToast(i18n, quasar);
    }
  }
}
</script>

<style scoped>

</style>
