<template>
  <q-page v-if="gym">
    <h3>{{ gym.displayName }}</h3>
    <p>Recent top lifts go here.</p>
    <q-btn
      color="primary"
      label="Submit Your Lift"
      :to="route.fullPath + '/submit'"
    />
    <p>All the rest of the gym leaderboards should show up here.</p>
  </q-page>
  <q-page v-if="notFound">
    <h3>Gym not found! Oh no!!!</h3>
    <router-link to="/">Back</router-link>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, Ref } from 'vue';
import { getGym, Gym } from 'src/api/gymboard-api';
import { useRoute } from 'vue-router';

const route = useRoute();

const gym: Ref<Gym | undefined> = ref<Gym>();
const notFound: Ref<boolean | undefined> = ref<boolean>();

// Once the component is mounted, load the gym that we're at.
onMounted(async () => {
  try {
    gym.value = await getGym(
      route.params.countryCode as string,
      route.params.cityShortName as string,
      route.params.gymShortName as string
    );
    notFound.value = false;
  } catch (error) {
    console.error(error);
    notFound.value = true;
  }
});
</script>