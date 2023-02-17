<template>
  <q-list separator>
    <q-item
      v-for="user in following" :key="user.id"
      :to="`/users/${user.id}`"
    >
      <q-item-section>
        <q-item-label>{{ user.name }}</q-item-label>
      </q-item-section>
    </q-item>
  </q-list>
</template>

<script setup lang="ts">
import {User} from "src/api/main/auth";
import {useAuthStore} from "stores/auth-store";
import {onMounted, ref, Ref} from "vue";
import api from 'src/api/main';

interface Props {
  user: User;
}
const props = defineProps<Props>();
const authStore = useAuthStore();
const following: Ref<User[]> = ref([]);

onMounted(async () => {
  following.value = await api.auth.getFollowing(props.user.id, authStore, 0, 10);
});
</script>

<style scoped>

</style>
