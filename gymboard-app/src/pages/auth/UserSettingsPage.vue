<template>
  <q-page>
    <StandardCenteredPage>
      <h3>{{ $t('userSettingsPage.title') }}</h3>
      <hr>
      <q-form>
        
      </q-form>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import { User } from 'src/api/main/auth';
import { getUserRoute } from 'src/router/user-routing';
import { useAuthStore } from 'src/stores/auth-store';
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  // If the user isn't on their own settings page, redirect them back to the user's profile page.
  const userId = route.params.userId as string;
  if (authStore.user?.id !== userId) {
    await router.push(`/users/${userId}`);
  }
});
</script>

<style scoped>
</style>