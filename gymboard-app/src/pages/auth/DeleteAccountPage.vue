<template>
  <q-page>
    <StandardCenteredPage>
      <h3>{{ $t('deleteAccountPage.title') }}</h3>
      <hr>
      <div v-if="contentVersion === 'en-US'">
        <p>
          On this page, you may choose to delete your Gymboard account. This
          action removes all Gymboard data associated with your account,
          permanently, without any possibility of recovery. Please consider
          carefully before proceeding.
        </p>
      </div>

      <div class="row justify-end">
        <q-btn
          :label="$t('deleteAccountPage.deleteButton')"
          color="secondary"
          @click="deleteAccount()"
          :disable="sent"
        />
      </div>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {computed, ref} from 'vue';
import {useI18n} from 'vue-i18n';
import {confirm, showApiErrorToast, showSuccessToast, sleep} from 'src/utils';
import api from 'src/api/main';
import {useAuthStore} from 'stores/auth-store';
import {useRouter} from 'vue-router';

const router = useRouter();
const i18n = useI18n();
const authStore = useAuthStore();

const sent = ref(false);

const contentVersion = computed(() => {
  if (i18n.locale.value === 'nl-NL') {
    // TODO: Add dutch translation!
  }
  return 'en-US';
});

async function deleteAccount() {
  confirm({
    title: i18n.t('deleteAccountPage.confirmTitle'),
    message: i18n.t('deleteAccountPage.confirmMessage')
  }).then(async () => {
    sent.value = true;
    try {
      await api.auth.deleteAccount(authStore);
      showSuccessToast('deleteAccountPage.accountDeleted');
      await sleep(1000);
      authStore.logOut();
      await router.push('/');
    } catch (error: any) {
      showApiErrorToast(error);
      await sleep(1000);
      sent.value = false;
    }
  });
}
</script>

<style scoped>

</style>
