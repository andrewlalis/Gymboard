<template>
  <q-page>
    <StandardCenteredPage v-if="gym">
      <h3 class="q-my-md text-center">{{ gym.displayName }}</h3>
      <PageMenu
        :base-route="getGymRoute(gym)"
        :items="[
          {label: t('gymPage.home')},
          {label: t('gymPage.submit'), to: 'submit'},
          {label: t('gymPage.leaderboard'), to: 'leaderboard'}
        ]"
      />
      <router-view />
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import {useRouter} from 'vue-router';
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {getGymFromRoute, getGymRoute} from 'src/router/gym-routing';
import {Gym} from 'src/api/main/gyms';
import PageMenu from 'components/PageMenu.vue';
import {useI18n} from 'vue-i18n';

const router = useRouter();
const t = useI18n().t;

const gym: Ref<Gym | undefined> = ref<Gym>();

// Once the component is mounted, load the gym that we're at.
onMounted(async () => {
  try {
    gym.value = await getGymFromRoute();
  } catch (error) {
    console.error(error);
    await router.push('/');
  }
});
</script>
