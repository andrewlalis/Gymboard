<template>
  <q-page>
    <StandardCenteredPage v-if="submission">
      <video
        class="submission-video"
        :src="getFileUrl(submission.videoFileId)"
        loop
        controls
        autopictureinpicture
        preload="metadata"
        autoplay
      />
      <h3>
          {{ submission.rawWeight }}&nbsp;{{ WeightUnitUtil.toAbbreviation(submission.weightUnit) }}
          {{ submission.exercise.displayName }}
      </h3>
      <p>{{ submission.reps }} reps</p>
      <p>by <router-link :to="'/users/' + submission.user.id">{{ submission.user.name }}</router-link></p>
      <p>At <router-link :to="getGymRoute(submission.gym)">{{ submission.gym.displayName }}</router-link></p>
      <p>
          {{ submission.performedAt.setLocale($i18n.locale).toLocaleString(DateTime.DATETIME_MED) }}
      </p>

      <!-- Deletion button is only visible if the user who submitted it is viewing it. -->
      <q-btn
        v-if="authStore.user && authStore.user.id === submission.user.id"
        label="Delete"
        @click="deleteSubmission"
      />
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import api from 'src/api/main';
import StandardCenteredPage from 'src/components/StandardCenteredPage.vue';
import { ExerciseSubmission, WeightUnitUtil } from 'src/api/main/submission';
import { onMounted, ref, Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DateTime } from 'luxon';
import { getFileUrl } from 'src/api/cdn';
import { getGymRoute } from 'src/router/gym-routing';
import {useAuthStore} from "stores/auth-store";
import {showApiErrorToast} from "src/utils";
import {useI18n} from "vue-i18n";
import {useQuasar} from "quasar";

const submission: Ref<ExerciseSubmission | undefined> = ref();

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const i18n = useI18n();
const quasar = useQuasar();

onMounted(async () => {
    const submissionId = route.params.submissionId as string;
    try {
        submission.value = await api.gyms.submissions.getSubmission(submissionId);
    } catch (error) {
        console.error(error);
        await router.push('/');
    }
});

async function deleteSubmission() {
  // TODO: Confirm via a dialog or something before deleting.
  if (!submission.value) return;
  try {
    await api.gyms.submissions.deleteSubmission(submission.value.id, authStore);
    await router.push('/');
  } catch (error) {
    console.error(error);
    showApiErrorToast(i18n, quasar);
  }
}
</script>
<style scoped>
.submission-video {
  width: 100%;
  max-height: 100%;
  margin-top: 20px;
}
</style>
