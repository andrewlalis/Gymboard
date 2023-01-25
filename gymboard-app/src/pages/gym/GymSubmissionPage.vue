<template>
  <q-page v-if="gym">
    <q-form @submit="onSubmitted">
      <SlimForm>
        <div class="row">
          <q-input
            :label="$t('gymPage.submitPage.name')"
            v-model="submissionModel.name"
            class="col-12"
          />
        </div>
        <div class="row">
          <q-select
            :options="exerciseOptions"
            map-options
            emit-value
            v-model="submissionModel.exerciseShortName"
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
            v-model="submissionModel.reps"
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
          <q-uploader
            :url="getUploadUrl(gym)"
            :label="$t('gymPage.submitPage.upload')"
            field-name="file"
            @uploaded="onFileUploaded"
            max-file-size="1000000000"
            class="col-12 q-mt-md"
          />
        </div>
        <div class="row">
          <q-btn
            :label="$t('gymPage.submitPage.submit')"
            color="primary"
            type="submit"
            class="q-mt-md col-12"
          />
        </div>
      </SlimForm>
    </q-form>
  </q-page>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import {
  createSubmission,
  Exercise,
  ExerciseSubmissionPayload,
  getExercises,
  getUploadUrl,
  Gym
} from 'src/api/gymboard-api';
import {getGymFromRoute} from 'src/router/gym-routing';
import SlimForm from 'components/SlimForm.vue';

interface Option {
  value: string,
  label: string
}

const gym: Ref<Gym | undefined> = ref<Gym>();
const exercises: Ref<Array<Exercise> | undefined> = ref<Array<Exercise>>();
const exerciseOptions: Ref<Array<Option>> = ref([]);
let submissionModel = ref({
  name: '',
  exerciseShortName: '',
  weight: 100,
  weightUnit: 'Kg',
  reps: 1,
  videoId: -1,
  date: new Date().toLocaleDateString('en-CA')
});
const weightUnits = ['Kg', 'Lbs'];

// TODO: Make it possible to pass the gym to this via props instead.
onMounted(async () => {
  try {
    gym.value = await getGymFromRoute();
  } catch (error) {
    console.error(error);
  }
  try {
    exercises.value = await getExercises();
    exerciseOptions.value = exercises.value.map(exercise => {
      return {value: exercise.shortName, label: exercise.displayName}
    });
  } catch (error) {
    console.error(error);
  }
});

function onFileUploaded(info: {files: Array<never>, xhr: XMLHttpRequest}) {
  const responseData = JSON.parse(info.xhr.responseText);
  submissionModel.value.videoId = responseData.id;
}

function onSubmitted() {
  console.log('submitted');
  if (gym.value) {
    const submission = createSubmission(gym.value, submissionModel.value);
    console.log(submission);
  }
}
</script>

<style scoped>

</style>
