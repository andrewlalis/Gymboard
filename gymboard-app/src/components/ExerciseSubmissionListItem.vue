<template>
  <q-item clickable :to="'/submissions/' + submission.id">
    <q-item-section>
      <q-item-label>
        {{ submission.rawWeight }}&nbsp;{{ WeightUnitUtil.toAbbreviation(submission.weightUnit) }}
        {{ submission.exercise.displayName }}
      </q-item-label>
      <q-item-label caption>
        {{ submission.submitterName }}
      </q-item-label>
    </q-item-section>
    <q-item-section side top>
      {{ submission.createdAt.setLocale($i18n.locale).toLocaleString(DateTime.DATETIME_MED) }}
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { ExerciseSubmission, WeightUnitUtil } from 'src/api/main/submission';
import { getFileUrl } from 'src/api/cdn';
import { DateTime } from 'luxon';

interface Props {
  submission: ExerciseSubmission;
}
defineProps<Props>();
</script>

<style scoped></style>
