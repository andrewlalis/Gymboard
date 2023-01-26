<template>
  <q-item :to="getGymRoute(gym)">
    <q-item-section>
      <q-item-label>{{ gym.displayName }}</q-item-label>
      <q-item-label caption lines="1">{{ gym.cityName }}</q-item-label>
      <q-item-label caption lines="1">{{ gym.countryName }}</q-item-label>
    </q-item-section>
    <q-item-section side top>
      <q-badge color="primary" label="10k" />
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { getGymRoute } from 'src/router/gym-routing';
import { GymSearchResult } from 'src/api/search/models';

interface Props {
  gym: GymSearchResult;
}
defineProps<Props>();
</script>

<style scoped></style>
