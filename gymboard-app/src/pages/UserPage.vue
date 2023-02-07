<template>
  <q-page>
    <StandardCenteredPage v-if="user">
      <h3>{{ user?.name }}</h3>
      <p>{{ user?.email }}</p>
      <p v-if="isOwnUser">This is your account!</p>
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

onMounted(async () => {
  const userId = route.params.userId as string;
  user.value = await api.auth.fetchUser(userId, authStore);
  isOwnUser.value = user.value.id === authStore.user?.id;
});
</script>

<style scoped>

</style>
