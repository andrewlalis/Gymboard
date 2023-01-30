<template>
  <StandardCenteredPage>
    <h3>Testing Page</h3>
    <p>
      Use this page to test new functionality, before adding it to the main app.
      This page should be hidden on production.
    </p>
    <div style="border: 3px solid red">
      <h4>Auth Test</h4>
      <q-btn label="Do auth" @click="doAuth()" />
      <q-btn label="Logout" @click="api.auth.logout(authStore)" />
    </div>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'src/components/StandardCenteredPage.vue';
import api from 'src/api/main';
import { useAuthStore } from 'stores/auth-store';

const authStore = useAuthStore();

async function doAuth() {
  await api.auth.login(authStore, {
    email: 'andrew.lalis@example.com',
    password: 'testpass',
  });
}
</script>

<style scoped></style>
