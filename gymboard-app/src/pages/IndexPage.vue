<template>
  <StandardCenteredPage>
    <q-input
      v-model="searchQuery"
      label="Search for a Gym"
      clearable
      :loading="searchBarLoadingState"
      @update:modelValue="onSearchQueryUpdated"
      class="q-mt-lg"
    >
      <template v-slot:append>
        <q-icon name="search" />
      </template>
    </q-input>
    <q-list>
      <SimpleGymItem v-for="gym in searchResults" :gym="gym" :key="gym.shortName" />
    </q-list>
  </StandardCenteredPage>
</template>

<script setup lang="ts">
import {onMounted, ref, Ref} from 'vue';
import SimpleGymItem from 'components/SimpleGymItem.vue';
import {useRoute, useRouter} from 'vue-router';
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {GymSearchResult, searchGyms} from 'src/api/gymboard-search';

const route = useRoute();
const router = useRouter();

const searchQuery: Ref<string> = ref('');
const searchBarLoadingState: Ref<boolean> = ref(false);
const searchResults: Ref<Array<GymSearchResult>> = ref([]);

let timer: ReturnType<typeof setTimeout> | null = null;

const SEARCH_TIMEOUT = 500;

onMounted(async () => {
  if (route.query.search_query) {
    searchQuery.value = route.query.search_query as string;
    searchBarLoadingState.value = true;
    await doSearch();
  }
});

/**
 * Function that's called when the user's search query updates. It will start
 * a timer that'll eventually trigger {@link doSearch}.
 */
function onSearchQueryUpdated() {
  if (timer !== null) {
    clearTimeout(timer);
  }
  timer = setTimeout(doSearch, SEARCH_TIMEOUT);
  searchBarLoadingState.value = true;
}

/**
 * Triggers a search of gyms using the current search query.
 */
async function doSearch() {
  const searchQueryText = searchQuery.value;
  let query = {};
  if (searchQueryText && searchQueryText.length > 0) {
    query = { search_query: searchQueryText };
  }
  await router.push({ path: '/', query: query });
  try {
    searchResults.value = await searchGyms(searchQueryText)
  } catch (error) {
    console.error(error);
  } finally {
    searchBarLoadingState.value = false;
  }
}
</script>
