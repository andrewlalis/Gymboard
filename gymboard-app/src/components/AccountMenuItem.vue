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
            <q-item-label>Log out</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-btn-dropdown>
    <q-btn
      color="primary"
      :label="$t('Login')"
      v-if="!authStore.loggedIn"
      no-caps
      icon="person"
      to="/login"
    />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from 'stores/auth-store';
import api from 'src/api/main';

const authStore = useAuthStore();
</script>

<style scoped></style>
