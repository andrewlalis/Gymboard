<template>
  <div class="q-mx-sm">
    <q-btn-dropdown
      color="primary"
      :label="authStore.user?.name"
      v-if="authStore.loggedIn"
      no-caps
      icon="person"
    >
      <q-list>
        <q-item clickable v-close-popup @click="api.auth.logout(authStore)">
          <q-item-section>
            <q-item-label>{{ $t('accountMenuItem.logOut') }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-btn-dropdown>
    <q-btn
      color="primary"
      :label="$t('accountMenuItem.logIn')"
      v-if="!authStore.loggedIn"
      no-caps
      icon="person"
      @click="goToLoginPage"
    />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from 'stores/auth-store';
import api from 'src/api/main';
import { useRoute, useRouter } from 'vue-router';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

async function goToLoginPage() {
  await router.push({
    path: '/login',
    query: {
      next: encodeURIComponent(route.path),
    },
  });
}
</script>

<style scoped></style>
