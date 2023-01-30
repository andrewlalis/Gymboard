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
        <LocaleSelect />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item-label header>
          {{ $t('mainLayout.pages') }}
        </q-item-label>
        <q-item clickable to="/">Gyms</q-item>
        <q-item clickable>Global Leaderboard</q-item>
        <q-item clickable to="/testing">Testing Page</q-item>
        <q-item clickable to="/about">About</q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import LocaleSelect from 'components/LocaleSelect.vue';
import AccountMenuItem from 'components/AccountMenuItem.vue';

const leftDrawerOpen = ref(false);

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}
</script>
