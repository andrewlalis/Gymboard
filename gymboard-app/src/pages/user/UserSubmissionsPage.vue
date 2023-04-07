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
import {useAuthStore} from 'stores/auth-store';
import {onMounted, ref, Ref} from 'vue';
import api from 'src/api/main';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';
import {showApiErrorToast} from 'src/utils';
import {PaginationHelpers} from 'src/api/main/models';
import InfinitePageLoader from 'src/api/infinite-page-loader';
import {Submission} from 'src/api/main/submission';

interface Props {
  userId: string;
}
const props = defineProps<Props>();

const authStore = useAuthStore();

const submissions: Ref<Submission[]> = ref([]);
const loader = new InfinitePageLoader(submissions, async paginationOptions => {
  try {
    return await api.users.getSubmissions(props.userId, authStore, paginationOptions);
  } catch (error) {
    showApiErrorToast(error);
  }
});

onMounted(async () => {
  loader.registerWindowScrollListener();
  await loader.setPagination(PaginationHelpers.sortedDescBy('properties.performedAt'));
});
</script>

<style scoped>

</style>
