<template>
  <q-page>
    Hello Gym Page
    <p>
        Leaderboard total
    </p>
  </q-page>
</template>

<script setup lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
const mountCount = ref(0);

onMounted(() => {
    console.log("mounted");
});
</script>