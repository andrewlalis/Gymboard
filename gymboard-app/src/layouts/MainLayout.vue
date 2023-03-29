<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title>
          <router-link to="/" style="text-decoration: none; color: inherit"
            >Gymboard</router-link
          >
        </q-toolbar-title>
        <AccountMenuItem />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item clickable to="/">
          <q-item-section>
            <q-item-label>Gyms</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable>
          <q-item-section>
            <q-item-label>Global Leaderboard</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable to="/users">
          <q-item-section>
            <q-item-label>Users</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable to="/admin" v-if="authStore.isAdmin">
          <q-item-section>
            <q-item-label>Admin Panel</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import AccountMenuItem from 'components/AccountMenuItem.vue';
import {useAuthStore} from 'stores/auth-store';

const authStore = useAuthStore();
const leftDrawerOpen = ref(false);

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}

onMounted(async () => {
  await authStore.tryLogInWithStoredToken();
});
</script>
