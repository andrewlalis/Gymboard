<template>
  <q-page v-if="gym" padding>
    <h3 class="q-mt-none">{{ gym.displayName }}</h3>
    <p>Recent top lifts go here.</p>
    
    <div class="q-pa-md" style="max-width: 400px">
      <q-form @submit="onSubmit" @reset="onReset" class="q-gutter-md">
        <h5>Submit your lift!</h5>
        <q-input
          v-model="submissionModel.name"
          filled
          label="Name"
        />
        <q-input
          v-model="submissionModel.exercise"
          filled
          label="Exercise"
        />
        <q-input
          v-model="submissionModel.weight"
          filled
          label="Weight"
        />
        <q-uploader
          :url="api.getUploadUrl(gym)"
          label="Upload video"
          field-name="file"
          max-file-size="1000000000"
          @uploaded="onUploadSuccess"
        >

        </q-uploader>
        <div>
          <q-btn label="Submit" type="submit" color="primary"/>
          <q-btn label="Reset" type="reset" color="primary" flat class="q-ml-sm"/>
        </div>
      </q-form>
    </div>

    <p>All the rest of the gym leaderboards should show up here.</p>
    <video v-if="videoRef" :src="api.getFileUrl(videoRef)"> </video>
  </q-page>
  <q-page v-if="notFound">
    <h3>Gym not found! Oh no!!!</h3>
    <router-link to="/">Back</router-link>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, Ref } from 'vue';
import * as api from 'src/api/gymboard-api';
import { useRoute } from 'vue-router';

const route = useRoute();

const gym: Ref<api.Gym | undefined> = ref<api.Gym>();
const notFound: Ref<boolean | undefined> = ref<boolean>();
const videoRef: Ref<number | undefined> = ref<number>();
let submissionModel = {
  name: '',
  exercise: '',
  weight: 0
};

// Once the component is mounted, load the gym that we're at.
onMounted(async () => {
  try {
    gym.value = await api.getGym(
      route.params.countryCode as string,
      route.params.cityShortName as string,
      route.params.gymShortName as string
    );
    notFound.value = false;
  } catch (error) {
    console.error(error);
    notFound.value = true;
  }
});

function onSubmit() {
  console.log('submitting!');
}

function onReset() {
  submissionModel.name = '';
  submissionModel.exercise = '';
  submissionModel.weight = 0;
}

function onUploadSuccess(info: any) {
  console.log(info);
  const fileId: number = JSON.parse(info.xhr.response).id;
  videoRef.value = fileId;
}
</script>