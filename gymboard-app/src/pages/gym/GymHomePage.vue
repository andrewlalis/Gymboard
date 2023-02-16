<template>
  <q-page v-if="gym">
    <div class="row">
      <div class="col-xs-12 col-md-6 q-pt-md">
        <p>{{ $t('gymPage.homePage.overview') }}</p>
        <ul>
          <li v-if="gym.websiteUrl">
            Website:
            <a :href="gym.websiteUrl" target="_blank">{{ gym.websiteUrl }}</a>
          </li>
          <li>
            Address: <em>{{ gym.streetAddress }}</em>
          </li>
          <li>
            City: <em>{{ gym.cityName }}</em>
          </li>
          <li>
            Country: <em>{{ gym.countryName }}</em>
          </li>
          <li>
            Registered at: <em>{{ gym.createdAt }}</em>
          </li>
        </ul>
      </div>
      <div class="col-xs-12 col-md-6">
        <div ref="mapContainer" style="height: 300px; width: 100%"></div>
      </div>
    </div>

    <div v-if="recentSubmissions.length > 0">
      <h4 class="text-center">{{ $t('gymPage.homePage.recentLifts') }}</h4>
      <q-list separator>
        <ExerciseSubmissionListItem
          v-for="sub in recentSubmissions"
          :submission="sub"
          :key="sub.id"
          :show-gym="false"
        />
      </q-list>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, Ref } from 'vue';
import { ExerciseSubmission } from 'src/api/main/submission';
import api from 'src/api/main';
import { getGymFromRoute } from 'src/router/gym-routing';
import ExerciseSubmissionListItem from 'components/ExerciseSubmissionListItem.vue';
import { Gym } from 'src/api/main/gyms';
import 'leaflet/dist/leaflet.css';
import { Map, Marker, TileLayer } from 'leaflet';

const recentSubmissions: Ref<Array<ExerciseSubmission>> = ref([]);
const gym: Ref<Gym | undefined> = ref();

const TILE_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>';
const map: Ref<Map | undefined> = ref();
const mapContainer = ref();

onMounted(async () => {
  gym.value = await getGymFromRoute();
  // We need to wait one tick for the main page to be loaded as a consequence of the gym being loaded.
  await nextTick(() => initMap());
  recentSubmissions.value = await api.gyms.getRecentSubmissions(gym.value);
});

function initMap() {
  if (!gym.value) return;
  const g: Gym = gym.value;

  const tiles = new TileLayer(TILE_URL, {
    attribution: ATTRIBUTION,
    maxZoom: 19,
  });
  const marker = new Marker([g.location.latitude, g.location.longitude], {
    title: g.displayName,
    alt: g.displayName,
  });
  map.value = new Map(mapContainer.value, {}).setView(
    [g.location.latitude, g.location.longitude],
    16
  );

  tiles.addTo(map.value);
  marker.addTo(map.value);

  setTimeout(() => {
    map.value?.invalidateSize();
  }, 400);
}
</script>
<style scoped></style>
