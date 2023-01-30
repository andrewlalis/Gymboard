<template>
  <q-expansion-item
    expand-separator
    :label="
      submission.rawWeight +
      ' ' +
      submission.weightUnit +
      ' x' +
      submission.reps +
      ' ' +
      submission.exercise.displayName
    "
    :caption="submission.submitterName"
  >
    <q-card>
      <q-card-section class="text-center">
        <video
          :src="api.gyms.submissions.getSubmissionVideoUrl(submission)"
          width="600"
          loop
          controls
          autopictureinpicture
          preload="metadata"
        />
      </q-card-section>
    </q-card>
  </q-expansion-item>
</template>

<script setup lang="ts">
import { ExerciseSubmission } from 'src/api/main/submission';
import api from 'src/api/main';

interface Props {
  submission: ExerciseSubmission;
}
defineProps<Props>();
</script>

<style scoped></style>
