<template>
  <q-page>
    <StandardCenteredPage v-if="authStore.loggedIn">
      <h3>{{ $t('updateEmailPage.title') }}</h3>
      <hr>

      <div v-if="!waitingForResetCode">
        <div class="row q-mt-md">
          <p>{{ $t('updateEmailPage.beforeUpdateInfo') }}</p>
        </div>

        <div class="row">
          <q-input
            type="email"
            v-model="email"
            :hint="$t('updateEmailPage.inputHint')"
            class="full-width"
            :readonly="waitingForResetCode"
          />
        </div>

        <div class="row justify-end">
          <q-btn
            color="primary"
            :label="$t('updateEmailPage.updateButton')"
            :disable="!updateButtonEnabled"
            @click="requestEmailCode()"
          />
        </div>
      </div>

      <div v-if="waitingForResetCode">
        <div class="row">
          <q-input
            type="text"
            v-model="resetCode"
            :hint="$t('updateEmailPage.resetCodeInputHint')"
            class="full-width"
            @change="resetCodeChanged()"
          />
        </div>
      </div>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">

import StandardCenteredPage from "components/StandardCenteredPage.vue";
import {useAuthStore} from "stores/auth-store";
import {computed, onBeforeMount, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import api from 'src/api/main';
import {showApiErrorToast, showSuccessToast, sleep} from "src/utils";

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const resetCode = ref('');
const waitingForResetCode = ref(false);

onBeforeMount(() => {
  if (!authStore.user) {
    router.replace('/');
    return;
  }
  email.value = authStore.user.email;
});

const updateButtonEnabled = computed(() => {
  return email.value &&
    email.value.trim().length > 3 &&
    email.value.trim() !== authStore.user?.email;
});

async function requestEmailCode() {
  try {
    await api.auth.generateEmailResetCode(email.value, authStore);
    waitingForResetCode.value = true;
    showSuccessToast('updateEmailPage.resetCodeSent');
  } catch (error: any) {
    showApiErrorToast(error);
  }
}

async function resetCodeChanged() {
  if (resetCode.value && resetCode.value.trim().length > 0) {
    const code = resetCode.value.trim();
    try {
      await api.auth.updateMyEmail(code, authStore);
      showSuccessToast('updateEmailPage.emailUpdated');
      await sleep(2000);
      authStore.logOut();
      await router.push('/login');
    } catch (error: any) {
      showApiErrorToast(error);
    }
  }
}
</script>
