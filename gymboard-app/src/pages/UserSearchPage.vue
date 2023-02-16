<template>
  <q-page>
    <StandardCenteredPage>
      <q-input
        v-model="searchQuery"
        :label="$t('userSearchPage.searchHint')"
        clearable
        :loading="searchBarLoadingState"
        @update:modelValue="onSearchQueryUpdated"
        class="q-mt-lg"
      >
        <template v-slot:append>
          <q-icon name="search"/>
        </template>
      </q-input>

      <q-list>
        <UserSearchResultListItem
          v-for="result in searchResults"
          :user="result"
          :key="result.id"
        />
      </q-list>
    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {onMounted, Ref, ref} from 'vue';
import {UserSearchResult} from 'src/api/search/models';
import {useRoute, useRouter} from 'vue-router';
import {sleep} from 'src/utils';
import searchApi from 'src/api/search';
import UserSearchResultListItem from 'components/UserSearchResultListItem.vue';

const route = useRoute();
const router = useRouter();

const searchQuery = ref('');
const searchBarLoadingState = ref(false);
const searchResults: Ref<UserSearchResult[]> = ref([]);

let timer: ReturnType<typeof setTimeout> | null = null;

const SEARCH_TIMEOUT = 500;

onMounted(async () => {
  if (route.query.search_query) {
    searchQuery.value = route.query.search_query as string;
    searchBarLoadingState.value = true;
    await sleep(500);
    await doSearch();
  }
});

function onSearchQueryUpdated() {
  if (timer !== null) {
    clearTimeout(timer);
  }
  timer = setTimeout(doSearch, SEARCH_TIMEOUT);
  searchBarLoadingState.value = true;
}

async function doSearch() {
  const searchQueryText = searchQuery.value;
  let query = {};
  if (searchQueryText && searchQueryText.length > 0) {
    query = { search_query: searchQueryText };
  }
  await router.push({ path: '/users', query: query });
  try {
    searchResults.value = await searchApi.searchUsers(searchQueryText);
  } catch (error) {
    console.error(error);
  } finally {
    searchBarLoadingState.value = false;
  }
}
</script>

<style scoped>

</style>
