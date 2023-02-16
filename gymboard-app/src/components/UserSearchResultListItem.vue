<template>
  <q-item :to="`/users/${user.id}`">
    <q-item-section>
      <q-item-label>{{ user.name }}</q-item-label>
    </q-item-section>
    <q-item-section side top>
      <q-badge color="primary" :label="submissionCountLabel"/>
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import {UserSearchResult} from 'src/api/search/models';
import {computed} from 'vue';

interface Props {
  user: UserSearchResult;
}
const props = defineProps<Props>();
const submissionCountLabel = computed(() => {
  const c = props.user.submissionCount;
  if (c < 1000) return '' + c;
  if (c < 1000000) return Math.floor(c / 1000) + 'k';
  return Math.floor(c / 1000000) + 'm';
});
</script>

<style scoped>

</style>
