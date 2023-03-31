<template>
  <q-page>
    <StandardCenteredPage>
      <h3>{{ $t('requestAccountDataPage.title') }}</h3>
      <hr>
      <div v-if="contentVersion === 'en-US'">
        <p>
          You have the right to issue a request for the data that Gymboard keeps
          for your user account. This may include, but is not limited to, basic
          user information (username, name, preferences, settings), historical
          data from previous user information you've provided, and lifting
          submission videos and their associated metadata.
        </p>
        <p>
          Gymboard makes a best effort to provide account data in a reasonable
          timeframe, while also accounting for the increased load this places on
          our services. Therefore, it may take up to <strong>7 days</strong> for
          your account data to be ready for download after issuing a request.
        </p>
        <p>
          Account data is formatted as compressed ZIP archive containing JSON
          files, as well as media files for any media you've uploaded. You will
          receive an email with a direct link to download the account data, once
          the request has been fulfilled. This link will expire after a few
          days, after which you must issue a new request to download your data.
        </p>
      </div>

      <div class="row justify-end">
        <q-btn
          :label="$t('requestAccountDataPage.requestButton')"
          color="secondary"
          @click="sendRequest()"
          :disable="sent"
        />
      </div>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {useI18n} from 'vue-i18n';
import {computed, ref} from 'vue';
import api from 'src/api/main';
import {useAuthStore} from 'stores/auth-store';
import {showApiErrorToast, showSuccessToast} from 'src/utils';

const i18n = useI18n();
const authStore = useAuthStore();

const sent = ref(false);

const contentVersion = computed(() => {
  if (i18n.locale.value === 'nl-NL') {
    // TODO: Add dutch translation!
  }
  return 'en-US';
});

async function sendRequest() {
  try {
    await api.auth.requestAccountData(authStore);
    showSuccessToast('requestAccountDataPage.requestSent');
    sent.value = true;
  } catch (error: any) {
    showApiErrorToast(error);
  }
}
</script>

<style scoped>

</style>
