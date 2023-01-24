<template>
  <q-page v-if="gym">
    <q-form @submit="onSubmitted">
      <SlimForm>
        <div class="row">
          <q-select
            :options="exercisesF"
            v-model="submissionModel.exercise"
            :label="$t('gymPage.submitPage.exercise')"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-input
            :label="$t('gymPage.submitPage.weight')"
            type="number"
            v-model="submissionModel.weight"
            class="col-8"
          />
          <q-select
            :options="weightUnits"
            v-model="submissionModel.weightUnit"
            class="col-4 q-pl-sm"
          />
        </div>
        <div class="row">
          <q-input
            :label="$t('gymPage.submitPage.reps')"
            type="number"
            v-model="submissionModel.repCount"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-input
            v-model="submissionModel.date"
            type="date"
            :label="$t('gymPage.submitPage.date')"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-btn
            :label="$t('gymPage.submitPage.submit')"
            color="primary"
            type="submit"
            class="q-mt-sm"
          />
        </div>
      </SlimForm>
    </q-form>
  </q-page>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import {Exercise, getExercises, Gym} from 'src/api/gymboard-api';
import {getGymFromRoute} from 'src/router/gym-routing';
import SlimForm from 'components/SlimForm.vue';

// interface Props {
//   gym: Gym
// }
// const props = defineProps<Props>();

const gym: Ref<Gym | undefined> = ref<Gym>();
const exercises: Ref<Array<Exercise> | undefined> = ref<Array<Exercise>>();
let submissionModel = ref({
  exercise: null,
  weight: null,
  weightUnit: 'Kg',
  repCount: 1,
  date: new Date().toLocaleDateString('en-CA')
});
const weightUnits = ['Kg', 'Lbs'];
const exercisesF = ['Bench Press', 'Squat', 'Deadlift'];

// TODO: Make it possible to pass the gym to this via props instead.
onMounted(async () => {
  try {
    gym.value = await getGymFromRoute();
  } catch (error) {
    console.error(error);
  }
  try {
    exercises.value = await getExercises();
  } catch (error) {
    console.error(error);
  }
});

function onSubmitted() {
  console.log('submitted');
}
</script>

<style scoped>

</style>
