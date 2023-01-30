<template>
  <q-page>
    <div class="q-ma-md row justify-end q-gutter-sm">
      <q-spinner color="primary" size="3em" v-if="loadingIndicatorActive" />
      <q-select
        v-model="selectedExercise"
        :options="exerciseOptions"
        :disable="loadingIndicatorActive"
        map-options
        emit-value
      />
      <q-select
        v-model="selectedTimeframe"
        :options="timeframeOptions"
        :disable="loadingIndicatorActive"
        map-options
        emit-value
      />
    </div>
    <q-list>
      <ExerciseSubmissionListItem
        v-for="sub in submissions"
        :submission="sub"
        :key="sub.id"
      />
    </q-list>
  </q-page>
</template>

<script setup lang="ts">
import api from 'src/api/main';
import { Exercise } from 'src/api/main/exercises';
import { Gym } from 'src/api/main/gyms';
import { LeaderboardTimeframe } from 'src/api/main/leaderboards';
import { ExerciseSubmission } from 'src/api/main/submission';
import ExerciseSubmissionListItem from 'src/components/ExerciseSubmissionListItem.vue';
import { getGymFromRoute } from 'src/router/gym-routing';
import { sleep } from 'src/utils';
import { onMounted, ref, Ref, watch, computed } from 'vue';

const submissions: Ref<Array<ExerciseSubmission>> = ref([]);
const gym: Ref<Gym | undefined> = ref();
const exercises: Ref<Array<Exercise>> = ref([]);

const exerciseOptions = computed(() => {
  let options = exercises.value.map((exercise) => {
    return {
      value: exercise.shortName,
      label: exercise.displayName,
    };
  });
  options.push({ value: '', label: 'Any' });
  return options;
});
const selectedExercise: Ref<string> = ref('');

const timeframeOptions = [
  { value: LeaderboardTimeframe.DAY, label: 'Day' },
  { value: LeaderboardTimeframe.WEEK, label: 'Week' },
  { value: LeaderboardTimeframe.MONTH, label: 'Month' },
  { value: LeaderboardTimeframe.YEAR, label: 'Year' },
  { value: LeaderboardTimeframe.ALL, label: 'All' },
];
const selectedTimeframe: Ref<LeaderboardTimeframe> = ref(
  LeaderboardTimeframe.DAY
);

const loadingIndicatorActive = ref(false);

onMounted(async () => {
  gym.value = await getGymFromRoute();
  exercises.value = await api.exercises.getExercises();
  doSearch();
});

async function doSearch() {
  submissions.value = [];
  if (gym.value) {
    loadingIndicatorActive.value = true;
    await sleep(500);
    submissions.value = await api.leaderboards.getLeaderboard({
      timeframe: selectedTimeframe.value,
      gyms: [gym.value],
      exerciseShortName: selectedExercise.value,
    });
    loadingIndicatorActive.value = false;
  }
}

watch([selectedTimeframe, selectedExercise], doSearch);
</script>

<style scoped></style>
