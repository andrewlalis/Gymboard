<template>
  <StandardCenteredPage>
    <h3 class="text-center">{{ statusText }}</h3>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from 'src/api/main';
import { sleep } from 'src/utils';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const route = useRoute();
const t = useI18n().t;

const statusText = ref('');
statusText.value = t('activationPage.activating');

onMounted(async () => {
  await sleep(500);
  const code = route.query.code as string;
  try {
    const user = api.auth.activateUser(code);
    statusText.value = t('activationPage.success');
    console.log(user);
    await sleep(2000);
    await router.replace('/login');
  } catch (error) {
    console.error(error);
    statusText.value = t('activationPage.failure');
  }
});
</script>

<style scoped></style>
