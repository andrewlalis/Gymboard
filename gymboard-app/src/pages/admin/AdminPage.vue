<!--
The main page for the admin panel that's only accessible to users with the
'admin' role.
-->
<template>
  <q-page>
    <StandardCenteredPage>
      <h3 class="q-my-md text-center">Admin Panel</h3>
      <PageMenu
        base-route="/admin"
        :items="[
          {label: 'Dashboard', to: ''}
        ]"
      />
      <router-view/>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import PageMenu from 'components/PageMenu.vue';
import {onMounted} from 'vue';

onMounted(async () => {
  console.log('admin');
});

</script>
