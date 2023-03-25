<template>
  <div>
    <div v-if="loadedSubmissions.length > 0">
      <q-list separator>
        <ExerciseSubmissionListItem
          v-for="sub in loadedSubmissions"
          :submission="sub"
          :key="sub.id"
          :show-name="false"
        />
      </q-list>
      <div class="text-center">
        <q-btn id="loadMoreButton" v-if="lastSubmissionsPage && !lastSubmissionsPage.last" @click="loadNextPage(true)">
          Load more
        </q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {useAuthStore} from 'stores/auth-store';
import {nextTick, onMounted, ref, Ref} from 'vue';
import {ExerciseSubmission} from 'src/api/main/submission';
import api from 'src/api/main';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';
import {showApiErrorToast} from 'src/utils';
import {Page, PaginationOptions, PaginationSortDir} from 'src/api/main/models';

interface Props {
  userId: string;
}
const props = defineProps<Props>();

const i18n = useI18n();
const quasar = useQuasar();
const authStore = useAuthStore();

const lastSubmissionsPage: Ref<Page<ExerciseSubmission> | undefined> = ref();
const loadedSubmissions: Ref<ExerciseSubmission[]> = ref([]);
const paginationOptions: PaginationOptions = {page: 0, size: 10};
onMounted(async () => {
  resetPagination();
  await loadNextPage(false);
});

async function loadNextPage(scroll: boolean) {
  try {
    lastSubmissionsPage.value = await api.users.getSubmissions(props.userId, authStore, paginationOptions);
    loadedSubmissions.value.push(...lastSubmissionsPage.value.content);
    paginationOptions.page++;
    await nextTick();
    const button = document.getElementById('loadMoreButton');
    if (scroll && button) {
      button.scrollIntoView({ behavior: 'smooth' });
    }
  } catch (error: any) {
    if (error.response) {
      showApiErrorToast(i18n, quasar);
    } else {
      console.log(error);
    }
  }
}

function resetPagination() {
  paginationOptions.page = 0;
  paginationOptions.size = 10;
  paginationOptions.sort = { propertyName: 'performedAt', sortDir: PaginationSortDir.DESC };
}
</script>

<style scoped>

</style>
