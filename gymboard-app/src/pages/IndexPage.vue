<template>
  <q-page>
    Index
    <router-link to="/gyms/a">Test</router-link>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'IndexPage',
  setup () {
    return {};
  }
});
</script>
