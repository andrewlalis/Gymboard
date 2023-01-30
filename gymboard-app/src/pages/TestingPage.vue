<template>
  <StandardCenteredPage>
    <h3>Testing Page</h3>
    <p>
      Use this page to test new functionality, before adding it to the main
      app. This page should be hidden on production.
    </p>
    <div class="row" style="border: 3px solid red">
      <h4>Auth Test</h4>
      <q-btn
        label="Do auth"
        @click="doAuth()"
      />
      <p>{{ authTestMessage }}</p>
    </div>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'src/components/StandardCenteredPage.vue';
import api from 'src/api/main';
import {ref} from 'vue';
import {sleep} from "src/utils";

const authTestMessage = ref('');

async function doAuth() {
  const token = await api.auth.getToken({email: 'andrew.lalis@example.com', password: 'testpass'});
  authTestMessage.value = 'Token: ' + token;
  await sleep(2000);
  const user = await api.auth.getMyUser(token);
  authTestMessage.value = 'User: ' + JSON.stringify(user);
}
</script>

<style scoped>

</style>
