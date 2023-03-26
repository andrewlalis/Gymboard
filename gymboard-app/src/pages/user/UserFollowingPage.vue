<template>
  <q-list separator>
    <q-item v-for="user in following" :key="user.id" :to="`/users/${user.id}`">
      <q-item-section>
        <q-item-label>{{ user.name }}</q-item-label>
      </q-item-section>
    </q-item>
  </q-list>
</template>

<script setup lang="ts">
import {User} from 'src/api/main/auth';
import {useAuthStore} from 'stores/auth-store';
import {onMounted, ref, Ref} from 'vue';
import api from 'src/api/main';
import InfinitePageLoader from 'src/api/infinite-page-loader';
import {defaultPaginationOptions} from 'src/api/main/models';

interface Props {
  userId: string;
}
const props = defineProps<Props>();
const authStore = useAuthStore();
const following: Ref<User[]> = ref([]);
const loader = new InfinitePageLoader(following, async paginationOptions => {
  try {
    return await api.auth.getFollowing(props.userId, authStore, paginationOptions);
  } catch (error) {
    console.log(error);
  }
});
onMounted(async () => {
  loader.registerWindowScrollListener();
  await loader.setPagination(defaultPaginationOptions());
});
</script>

<style scoped></style>
