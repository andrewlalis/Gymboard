<template>
  <q-page>
    <StandardCenteredPage>
      <h3>{{ $t('userSettingsPage.title') }}</h3>
      <hr>
      <div v-if="personalDetails">
        <h4>Personal Information</h4>
        <EditablePropertyRow
          v-model="personalDetails.birthDate"
          :label="$t('userSettingsPage.personalDetails.birthDate')"
          input-type="date"
        />

        <div v-if="personalDetailsChanged">
          <q-btn :label="$t('userSettingsPage.save')" color="positive" @click="savePersonalDetails"/>
          <q-btn :label="$t('userSettingsPage.undo')" color="secondary" @click="undoPersonalDetailsChanges"/>
        </div>

      </div>

      <div v-if="preferences">
        <h4>Preferences</h4>

        <EditablePropertyRow
          v-model="preferences.accountPrivate"
          :label="$t('userSettingsPage.preferences.accountPrivate')"
        />

        <div v-if="preferencesChanged">
          <q-btn :label="$t('userSettingsPage.save')" color="positive" @click="savePreferences"/>
          <q-btn :label="$t('userSettingsPage.undo')" color="secondary" @click="undoPreferencesChanges"/>
        </div>
      </div>

    </StandardCenteredPage>
  </q-page>
</template>

<script setup lang="ts">
import StandardCenteredPage from 'components/StandardCenteredPage.vue';
import {useRoute, useRouter} from 'vue-router';
import {useAuthStore} from 'stores/auth-store';
import {computed, onMounted, ref, Ref, toRaw} from 'vue';
import {UserPersonalDetails, UserPreferences} from 'src/api/main/auth';
import api from 'src/api/main';
import EditablePropertyRow from 'components/EditablePropertyRow.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const personalDetails: Ref<UserPersonalDetails | undefined> = ref();
const preferences: Ref<UserPreferences | undefined> = ref();

let initialPersonalDetails: UserPersonalDetails | null = null;
let initialPreferences: UserPreferences | null = null;

onMounted(async () => {
  // Redirect away from the page if the user isn't viewing their own settings.
  const userId = route.params.userId as string;
  if (!authStore.user || authStore.user.id !== userId) {
    await router.push(`/users/${userId}`);
  }

  personalDetails.value = await api.auth.getMyPersonalDetails(authStore);
  initialPersonalDetails = structuredClone(toRaw(personalDetails.value));

  preferences.value = await api.auth.getMyPreferences(authStore);
  initialPreferences = structuredClone(toRaw(preferences.value));
});

const personalDetailsChanged = computed(() => {
  return initialPersonalDetails !== null &&
    JSON.stringify(initialPersonalDetails) !== JSON.stringify(personalDetails.value);
});

const preferencesChanged = computed(() => {
  return initialPreferences !== null &&
    JSON.stringify(initialPreferences) !== JSON.stringify(preferences.value);
});

async function savePersonalDetails() {
  if (personalDetails.value) {
    personalDetails.value = await api.auth.updateMyPersonalDetails(authStore, personalDetails.value);
    initialPersonalDetails = structuredClone(toRaw(personalDetails.value));
  }
}

function undoPersonalDetailsChanges() {
  personalDetails.value = structuredClone(initialPersonalDetails);
}

async function savePreferences() {
  if (preferences.value) {
    preferences.value = await api.auth.updateMyPreferences(authStore, preferences.value);
    initialPreferences = structuredClone(toRaw(preferences.value));
  }
}

function undoPreferencesChanges() {
  preferences.value = structuredClone(initialPreferences);
}
</script>

<style scoped>

</style>
