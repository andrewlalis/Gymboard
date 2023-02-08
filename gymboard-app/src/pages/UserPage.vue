<template>
  <q-page>
    <StandardCenteredPage v-if="user">
      <h3>{{ user?.name }}</h3>
      <p>{{ user?.email }}</p>
      <p v-if="isOwnUser">This is your account!</p>
    </StandardCenteredPage>
    <StandardCenteredPage v-if="userNotFound">
      <h3>{{ $t('userPage.notFound.title') }}</h3>
      <p>{{ $t('userPage.notFound.description') }}</p>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {onMounted, ref, Ref} from 'vue';
import {User} from 'src/api/main/auth';
import api from 'src/api/main';
import {useRoute} from 'vue-router';
import {useAuthStore} from 'stores/auth-store';

const route = useRoute();
const authStore = useAuthStore();

/**
 * The user that this page displays information about.
 */
const user: Ref<User | undefined> = ref();

/**
 * Flag that tells whether this user is the currently authenticated user.
 */
const isOwnUser = ref(false);

/**
 * Flag used to indicate whether we should show a "not found" message instead
 * of the usual user page.
 */
const userNotFound = ref(false);

onMounted(async () => {
  const userId = route.params.userId as string;
  try {
    user.value = await api.auth.getUser(userId, authStore);
  } catch (error: any) {
    if (error.response && error.response.code === 404) {
      userNotFound.value = true;
    }
  }
  isOwnUser.value = authStore.loggedIn && user.value.id === authStore.user?.id;
});
</script>

<style scoped>

</style>
