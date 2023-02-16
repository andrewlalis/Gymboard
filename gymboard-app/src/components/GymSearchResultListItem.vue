<template>
  <q-item :to="getGymRoute(gym)">
    <q-item-section>
      <q-item-label>{{ gym.displayName }}</q-item-label>
      <q-item-label caption lines="1">{{ gym.cityName }}</q-item-label>
      <q-item-label caption lines="1">{{ gym.countryName }}</q-item-label>
    </q-item-section>
    <q-item-section side top>
      <q-badge color="primary" :label="submissionCountLabel" />
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { getGymRoute } from 'src/router/gym-routing';
import { GymSearchResult } from 'src/api/search/models';
import {computed} from 'vue';

interface Props {
  gym: GymSearchResult;
}
const props = defineProps<Props>();
const submissionCountLabel = computed(() => {
  const c = props.gym.submissionCount;
  if (c < 1000) return '' + c;
  if (c < 1000000) return Math.floor(c / 1000) + 'k';
  return Math.floor(c / 1000000) + 'm';
});
</script>

<style scoped></style>
