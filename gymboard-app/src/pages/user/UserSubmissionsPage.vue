<template>
  <div>
    <div v-if="recentSubmissions.length > 0">
      <q-list separator>
        <ExerciseSubmissionListItem
          v-for="sub in recentSubmissions"
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
import {ExerciseSubmission} from 'src/api/main/submission';
import {User} from 'src/api/main/auth';
import api from 'src/api/main';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';
import {showApiErrorToast} from 'src/utils';

interface Props {
  user: User;
}
const props = defineProps<Props>();

const i18n = useI18n();
const quasar = useQuasar();
const authStore = useAuthStore();

const recentSubmissions: Ref<ExerciseSubmission[]> = ref([]);
onMounted(async () => {
  try {
    recentSubmissions.value = await api.users.getRecentSubmissions(props.user.id, authStore);
  } catch (error: any) {
    showApiErrorToast(i18n, quasar);
  }
});
</script>

<style scoped>

</style>
