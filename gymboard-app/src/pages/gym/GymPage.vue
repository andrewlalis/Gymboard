<template>
  <StandardCenteredPage v-if="gym">
    <h3 class="q-my-md text-center">{{ gym.displayName }}</h3>
    <q-btn-group spread square push>
      <q-btn
        :label="$t('gymPage.home')"
        :to="getGymRoute(gym)"
        :color="homePageSelected ? 'primary' : 'secondary'"
      />
      <q-btn
        :label="$t('gymPage.submit')"
        :to="getGymRoute(gym) + '/submit'"
        :color="submitPageSelected ? 'primary' : 'secondary'"
      />
      <q-btn
        :label="$t('gymPage.leaderboard')"
        :to="getGymRoute(gym) + '/leaderboard'"
        :color="leaderboardPageSelected ? 'primary' : 'secondary'"
      />
    </q-btn-group>
    <router-view/>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, Ref} from 'vue';
import { getGym, Gym } from 'src/api/gymboard-api';
import {useRoute, useRouter} from 'vue-router';
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {getGymRoute} from 'src/router/gym-routing';

const route = useRoute();
const router = useRouter();

const gym: Ref<Gym | undefined> = ref<Gym>();

// Once the component is mounted, load the gym that we're at.
onMounted(async () => {
  try {
    gym.value = await getGym(
      route.params.countryCode as string,
      route.params.cityShortName as string,
      route.params.gymShortName as string
    );
  } catch (error) {
    console.error(error);
    await router.push('/');
  }
});

const homePageSelected = computed(() => gym.value && getGymRoute(gym.value) === route.fullPath);
const submitPageSelected = computed(() => gym.value && route.fullPath === getGymRoute(gym.value) + '/submit');
const leaderboardPageSelected = computed(() => gym.value && route.fullPath === getGymRoute(gym.value) + '/leaderboard');
</script>
