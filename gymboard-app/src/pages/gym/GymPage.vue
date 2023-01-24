<template>
  <StandardCenteredPage v-if="gym">
    <h3 class="q-my-sm">{{ gym.displayName }}</h3>
    <q-btn-group>
      <q-btn label="Home" :to="baseGymPath"/>
      <q-btn label="Submit" :to="baseGymPath + '/submit'"/>
      <q-btn label="Leaderboard" :to="baseGymPath + '/lb'"/>
    </q-btn-group>
    <router-view/>
  </StandardCenteredPage>
  <q-page v-if="notFound">
    <h3>Gym not found! Oh no!!!</h3>
    <router-link to="/">Back</router-link>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, Ref } from 'vue';
import { getGym, Gym } from 'src/api/gymboard-api';
import { useRoute } from 'vue-router';
import StandardCenteredPage from 'components/StandardCenteredPage.vue';

const route = useRoute();
const baseGymPath = `/g/${route.params.countryCode}/${route.params.cityShortName}/${route.params.gymShortName}`;

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
