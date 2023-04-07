<template>
  <q-item clickable :to="'/submissions/' + submission.id">
    <q-item-section>
      <q-item-label>
        {{ submission.rawWeight }}&nbsp;{{ WeightUnitUtil.toAbbreviation(submission.weightUnit) }}
        {{ submission.exercise.displayName }}
      </q-item-label>
      <q-item-label caption v-if="showName">
        {{ submission.user.name }}
      </q-item-label>
      <q-item-label caption v-if="showGym">
        {{ submission.gym.displayName }}
      </q-item-label>
    </q-item-section>
    <q-item-section side top>
      {{ submission.performedAt.setLocale($i18n.locale).toLocaleString(DateTime.DATETIME_MED) }}
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { Submission, WeightUnitUtil } from 'src/api/main/submission';
import { DateTime } from 'luxon';

interface Props {
  submission: Submission;
  showName?: boolean;
  showGym?: boolean;
}
withDefaults(defineProps<Props>(), {
  showName: true,
  showGym: true,
});
</script>

<style scoped></style>
