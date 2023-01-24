<template>
  <q-page v-if="gym">
    <h3>Submit Your Lift for {{ gym.displayName }}</h3>
    <q-form>

    </q-form>
  </q-page>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import {getGym, Gym} from 'src/api/gymboard-api';
import {useRoute} from 'vue-router';

const route = useRoute();
const gym: Ref<Gym | undefined> = ref<Gym>();

onMounted(async () => {
  try {
    gym.value = await getGym(
      route.params.countryCode as string,
      route.params.cityShortName as string,
      route.params.gymShortName as string
    );
  } catch (error) {
    console.error(error);
  }
})
</script>

<style scoped>

</style>
