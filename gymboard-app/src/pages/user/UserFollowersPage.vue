<template>
  <q-list separator>
    <q-item
      v-for="user in followers" :key="user.id"
      :to="`/users/${user.id}`"
    >
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

interface Props {
  userId: string
}
const props = defineProps<Props>();
const authStore = useAuthStore();
const followers: Ref<User[]> = ref([]);

onMounted(async () => {
  followers.value = await api.auth.getFollowers(props.userId, authStore, 0, 10);
});
</script>

<style scoped>

</style>
