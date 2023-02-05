<template>
    <q-page>
        <standard-centered-page v-if="submission">
            <h3>Submission: {{ submission.id }}</h3>
        </standard-centered-page>
    </q-page>
</template>

<script setup lang="ts">
import api from 'src/api/main';
import { ExerciseSubmission } from 'src/api/main/submission';
import { onMounted, ref, Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const submission: Ref<ExerciseSubmission | undefined> = ref();

const route = useRoute();
const router = useRouter();

onMounted(async () => {
    const submissionId = route.params.submissionId as string;
    try {
        submission.value = await api.gyms.submissions.getSubmission(submissionId);
    } catch (error) {
        console.error(error);
        await router.push('/');
    }
});
</script>