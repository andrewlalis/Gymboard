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
  <q-page v-if="gym && authStore.loggedIn">
    <!-- The below form contains the fields that will become part of the submission. -->
    <q-form @submit="onSubmitted">
      <SlimForm>
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
            v-model="submissionModel.performedAt"
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
            :label="submitButtonLabel"
            color="primary"
            type="submit"
            class="q-mt-md col-12"
            :disable="!submitButtonEnabled()"
          />
        </div>
      </SlimForm>
    </q-form>
  </q-page>

  <!-- If the user is not logged in, show a link to log in. -->
  <q-page v-if="!authStore.loggedIn">
    <div class="q-mt-lg text-center">
      <router-link :to="`/login?next=${route.fullPath}`" class="text-primary"
        >Login or register to submit your lift</router-link
      >
    </div>
  </q-page>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import {getGymFromRoute} from 'src/router/gym-routing';
import SlimForm from 'components/SlimForm.vue';
import api from 'src/api/main';
import {Gym} from 'src/api/main/gyms';
import {Exercise} from 'src/api/main/exercises';
import {useRoute, useRouter} from 'vue-router';
import {showApiErrorToast, showWarningToast, sleep} from 'src/utils';
import {uploadVideoToCDN,} from 'src/api/cdn';
import {useAuthStore} from 'stores/auth-store';
import {useI18n} from 'vue-i18n';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const i18n = useI18n();

interface Option {
  value: string;
  label: string;
}

const gym: Ref<Gym | undefined> = ref<Gym>();
const exercises: Ref<Array<Exercise> | undefined> = ref<Array<Exercise>>();
const exerciseOptions: Ref<Array<Option>> = ref([]);
let submissionModel = ref({
  exerciseShortName: '',
  weight: 100,
  weightUnit: 'Kg',
  reps: 1,
  performedAt: new Date().toLocaleDateString('en-CA'),
  taskId: -1
});
const selectedVideoFile: Ref<File | undefined> = ref<File>();
const weightUnits = ['KG', 'LBS'];

const submitting = ref(false);
const submitButtonLabel = ref(i18n.t('gymPage.submitPage.submit'));

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
  return (
    selectedVideoFile.value !== undefined && !submitting.value && validateForm()
  );
}

function validateForm() {
  return true;
}

/**
 * Runs through the entire submission process.
 */
async function onSubmitted() {
  if (!selectedVideoFile.value || !gym.value) return;

  submitting.value = true;
  if (await uploadVideo()) {
    await createSubmission();
  }
  submitting.value = false;
}

/**
 * Uploads the selected video and returns true if successful.
 */
async function uploadVideo(): Promise<boolean> {
  if (!selectedVideoFile.value) return false;
  try {
    // 1. Upload the video to the CDN.
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submitUploading');
    await sleep(1000);
    submissionModel.value.taskId = await uploadVideoToCDN(selectedVideoFile.value);
    return true;
  } catch (error) {
    showApiErrorToast(error);
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submitUploadFailed');
    await sleep(1000);
    selectedVideoFile.value = undefined;
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submit');
    return false;
  }
}

/**
 * Tries to create a new submission, and if successful, redirects the user to it.
 */
async function createSubmission() {
  if (!gym.value) return;
  try {
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submitCreatingSubmission');
    await sleep(1000);
    const submission = await api.gyms.submissions.createSubmission(gym.value, submissionModel.value, authStore);
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submitComplete');
    await sleep(2000);
    await router.push(`/submissions/${submission.id}`);
  } catch (error: any) {
    if (error.response && error.response.status === 400) {
      showWarningToast(error.response.data.message);
      submitButtonLabel.value = i18n.t('gymPage.submitPage.submitFailed');
    } else {
      showApiErrorToast(error);
    }
    await sleep(3000);
    submitButtonLabel.value = i18n.t('gymPage.submitPage.submit');
  }
}
</script>

<style scoped></style>
