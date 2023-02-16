<template>
  <q-page>
    <StandardCenteredPage v-if="user">
      <h3>{{ user?.name }}</h3>

      <p>{{ user?.email }}</p>
      <p v-if="isOwnUser">This is your account!</p>

      <hr>

      <div v-if="userPrivate">
        This account is private.
      </div>

      <div v-if="recentSubmissions.length > 0">
        <h4 class="text-center">{{ $t('userPage.recentLifts') }}</h4>
        <q-list separator>
          <ExerciseSubmissionListItem
            v-for="sub in recentSubmissions"
            :submission="sub"
            :key="sub.id"
            :show-name="false"
          />
        </q-list>
      </div>

    </StandardCenteredPage>
    <StandardCenteredPage v-if="userNotFound">
      <h3>{{ $t('userPage.notFound.title') }}</h3>
      <p>{{ $t('userPage.notFound.description') }}</p>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {onMounted, ref, Ref} from 'vue';
import {User} from 'src/api/main/auth';
import api from 'src/api/main';
import {useRoute} from 'vue-router';
import {useAuthStore} from 'stores/auth-store';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {showApiErrorToast} from 'src/utils';
import {ExerciseSubmission} from 'src/api/main/submission';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';

const route = useRoute();
const authStore = useAuthStore();
const i18n = useI18n();
const quasar = useQuasar();

/**
 * The user that this page displays information about.
 */
const user: Ref<User | undefined> = ref();
const recentSubmissions: Ref<ExerciseSubmission[]> = ref([]);
const isOwnUser = ref(false);
const userNotFound = ref(false);
const userPrivate = ref(false);

onMounted(async () => {
  const userId = route.params.userId as string;
  try {
    user.value = await api.auth.getUser(userId, authStore);
  } catch (error: any) {
    if (error.response && error.response.status === 404) {
      userNotFound.value = true;
    } else {
      showApiErrorToast(i18n, quasar);
    }
  }
  isOwnUser.value = authStore.loggedIn && user.value?.id === authStore.user?.id;

  // If the user exists, try and fetch their latest submissions, and handle a 403 forbidden as private.
  if (user.value) {
    try {
      recentSubmissions.value = await api.users.getRecentSubmissions(user.value?.id, authStore);
    } catch (error: any) {
      if (error.response && error.response.status === 403) {
        userPrivate.value = true;
      } else {
        showApiErrorToast(i18n, quasar);
      }
    }
  }
});
</script>

<style scoped>

</style>
