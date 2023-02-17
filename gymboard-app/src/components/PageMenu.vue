<!--
A menu for a page, in which items refer to different sub-pages.
-->
<template>
  <q-btn-group spread square push>
    <q-btn
      v-for="(item, index) in items" :key="index"
      :label="item.label"
      :to="getItemPath(item)"
      :color="isItemSelected(index) ? 'primary' : 'secondary'"
    />
  </q-btn-group>
</template>

<script setup lang="ts">

import {useRoute} from 'vue-router';

const route = useRoute();

interface MenuItem {
  label: string;
  to?: string;
}
interface Props {
  items: MenuItem[];
  baseRoute: string;
}
const props = defineProps<Props>();

function isItemSelected(index: number): boolean {
  const item = props.items[index];
  return route.path === getItemPath(item);
}

function getItemPath(item: MenuItem): string {
  if (!item.to || item.to === '') return props.baseRoute;
  return props.baseRoute + '/' + item.to;
}
</script>

<style scoped>

</style>
