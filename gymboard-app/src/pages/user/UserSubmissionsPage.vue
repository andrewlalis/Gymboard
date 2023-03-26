<template>
  <div>
    <div v-if="submissions.length > 0">
      <q-list separator>
        <ExerciseSubmissionListItem
          v-for="sub in submissions"
          :submission="sub"
          :key="sub.id"
          :show-name="false"
        />
      </q-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {useAuthStore} from 'stores/auth-store';
import {onMounted, ref, Ref} from 'vue';
import api from 'src/api/main';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';
import {showApiErrorToast} from 'src/utils';
import {PaginationHelpers} from 'src/api/main/models';
import InfinitePageLoader from 'src/api/infinite-page-loader';
import {ExerciseSubmission} from 'src/api/main/submission';

interface Props {
  userId: string;
}
const props = defineProps<Props>();

const i18n = useI18n();
const quasar = useQuasar();
const authStore = useAuthStore();

const submissions: Ref<ExerciseSubmission[]> = ref([]);
const loader = new InfinitePageLoader(submissions, async paginationOptions => {
  try {
    return await api.users.getSubmissions(props.userId, authStore, paginationOptions);
  } catch (error: any) {
    if (error.response) {
      showApiErrorToast(i18n, quasar);
    } else {
      console.log(error);
    }
  }
});

onMounted(async () => {
  loader.registerWindowScrollListener();
  await loader.setPagination(PaginationHelpers.sortedDescBy('performedAt'));
});
</script>

<style scoped>

</style>
