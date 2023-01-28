<!--
This is the submission page, where users can submit their lift to a particular
gym's leaderboards.

A high-level overview of the submission process is as follows:

1. The user uploads a video of their lift, and receives a `videoId` in return.
2. The user submits their lift's JSON data, including the `videoId`.
3. The API responds (if the data is valid) with the created submission, with the status WAITING.
4. Eventually the API will process the submission and status will change to either COMPLETED or FAILED.
5. We wait on the submission page until the submission is done processing, then show a message and navigate to the submission page.
-->
<template>
  <q-page v-if="gym">
    <!-- The below form contains the fields that will become part of the submission. -->
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
          <q-file
            v-model="selectedVideoFile"
            :label="$t('gymPage.submitPage.upload')"
            max-file-size="1000000000"
            accept="video/*"
            class="col-12"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>
        </div>
        <div class="row">
          <q-btn
            :label="$t('gymPage.submitPage.submit')"
            color="primary"
            type="submit"
            class="q-mt-md col-12"
            :disable="!submitButtonEnabled()"
          />
        </div>
        <div class="row text-center" v-if="infoMessage">
          <p>{{ infoMessage }}</p>
        </div>
      </SlimForm>
    </q-form>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, Ref } from 'vue';
import {getGymFromRoute, getGymRoute} from 'src/router/gym-routing';
import SlimForm from 'components/SlimForm.vue';
import api from 'src/api/main';
import { Gym } from 'src/api/main/gyms';
import { Exercise } from 'src/api/main/exercises';
import {useRouter} from 'vue-router';
import { sleep } from 'src/utils';

interface Option {
  value: string;
  label: string;
}

const router = useRouter();

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
  videoFile: null,
  date: new Date().toLocaleDateString('en-CA'),
});
const selectedVideoFile: Ref<File | undefined> = ref<File>();
const weightUnits = ['KG', 'LBS'];

const submitting = ref(false);
const infoMessage: Ref<string | undefined> = ref();

onMounted(async () => {
  try {
    gym.value = await getGymFromRoute();
  } catch (error) {
    console.error(error);
  }
  try {
    exercises.value = await api.exercises.getExercises();
    exerciseOptions.value = exercises.value.map((exercise) => {
      return { value: exercise.shortName, label: exercise.displayName };
    });
  } catch (error) {
    console.error(error);
  }
});

function submitButtonEnabled() {
  return selectedVideoFile.value !== undefined && !submitting.value && validateForm();
}

function validateForm() {
  return true;
}

async function onSubmitted() {
  if (!selectedVideoFile.value || !gym.value) throw new Error('Invalid state.');
  submitting.value = true;
  try {
    infoMessage.value = 'Uploading video...';
    await sleep(1000);
    submissionModel.value.videoId = await api.gyms.submissions.uploadVideoFile(
      gym.value,
      selectedVideoFile.value
    );
    infoMessage.value = 'Creating submission...';
    await sleep(1000);
    const submission = await api.gyms.submissions.createSubmission(
      gym.value,
      submissionModel.value
    );
    infoMessage.value = 'Submission processing...';
    const completedSubmission =
      await api.gyms.submissions.waitUntilSubmissionProcessed(submission.id);
    console.log(completedSubmission);
    infoMessage.value = 'Submission complete!';
    await router.push(getGymRoute(gym.value));
  } finally {
    submitting.value = false;
  }
  
}
</script>

<style scoped></style>
