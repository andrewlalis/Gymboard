<!--
An item that's meant for the main UI menu, and features a dropdown with some
account-related actions.
-->
<template>
  <div class="q-mx-sm">
    <q-btn-dropdown
      color="primary"
      :label="authStore.user?.name"
      v-if="authStore.loggedIn && authStore.user"
      no-caps
      icon="person"
    >
      <q-list>
        <q-item clickable v-close-popup :to="getUserRoute(authStore.user)">
          <q-item-section>
            <q-item-label>{{ $t('accountMenuItem.profile') }}</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable v-close-popup :to="getUserRoute(authStore.user) + '/settings'">
          <q-item-section>
            <q-item-label>{{ $t('accountMenuItem.settings') }}</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable v-close-popup @click="authStore.logOut()">
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
import { useRoute, useRouter } from 'vue-router';
import { getUserRoute } from 'src/router/user-routing';

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
