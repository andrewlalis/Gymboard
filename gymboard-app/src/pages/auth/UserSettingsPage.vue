<!--
The page where users can edit their personal information and preferences.
-->
<template>
  <q-page>
    <StandardCenteredPage v-if="authStore.loggedIn">
      <h3>{{ $t('userSettingsPage.title') }}</h3>
      <hr>

      <div class="row justify-between">
        <span class="property-label">{{ $t('userSettingsPage.email') }}</span>
        <q-input type="email" v-model="authStore.user.email" dense readonly/>
      </div>
      <div class="row justify-between">
        <span class="property-label">{{ $t('userSettingsPage.name') }}</span>
        <q-input type="text" v-model="authStore.user.name" dense readonly/>
      </div>
      <div class="row justify-between">
        <span class="property-label">{{ $t('userSettingsPage.password') }}</span>
        <q-input
          dense
          type="password"
          v-model="newPassword"
          :hint="$t('userSettingsPage.passwordHint')"
        />
      </div>
      <div>
        <q-btn v-if="canUpdatePassword" :label="$t('userSettingsPage.updatePassword')" color="positive" @click="updatePassword"/>
      </div>

      <div v-if="personalDetails">
        <h4>{{ $t('userSettingsPage.personalDetails.title') }}</h4>
        <div v-if="personalDetailsChanged" class="save-button-row">
          <q-btn :label="$t('userSettingsPage.save')" color="positive" @click="savePersonalDetails"/>
          <q-btn :label="$t('userSettingsPage.undo')" color="secondary" @click="undoPersonalDetailsChanges"/>
        </div>
        <EditablePropertyRow
          v-model="personalDetails.birthDate"
          :label="$t('userSettingsPage.personalDetails.birthDate')"
          input-type="date"
        />
        <EditablePropertyRow
          v-model="personalDetails.sex"
          :label="$t('userSettingsPage.personalDetails.sex')"
          input-type="select"
          :select-options="[
            { label: $t('userSettingsPage.personalDetails.sexMale'), value: 'MALE' },
            { label: $t('userSettingsPage.personalDetails.sexFemale'), value: 'FEMALE' },
            { label: $t('userSettingsPage.personalDetails.sexUnknown'), value: 'UNKNOWN' },
          ]"
        />
        <EditablePropertyRow
          v-model="personalDetails.currentWeight"
          :label="$t('userSettingsPage.personalDetails.currentWeight')"
          input-type="number"
          :number-input-step="0.1"
        />
        <EditablePropertyRow
          v-model="personalDetails.currentWeightUnit"
          :label="$t('userSettingsPage.personalDetails.currentWeightUnit')"
          input-type="select"
          :select-options="[
            { label: $t('weightUnit.kilograms'), value: WeightUnit.KILOGRAMS },
            { label: $t('weightUnit.pounds'), value: WeightUnit.POUNDS },
          ]"
        />

      </div>

      <div v-if="preferences">
        <h4>{{ $t('userSettingsPage.preferences.title') }}</h4>
        <div v-if="preferencesChanged" class="save-button-row">
          <q-btn :label="$t('userSettingsPage.save')" color="positive" @click="savePreferences"/>
          <q-btn :label="$t('userSettingsPage.undo')" color="secondary" @click="undoPreferencesChanges"/>
        </div>
        <EditablePropertyRow
          v-model="preferences.accountPrivate"
          :label="$t('userSettingsPage.preferences.accountPrivate')"
        />
        <EditablePropertyRow
          v-model="preferences.locale"
          :label="$t('userSettingsPage.preferences.language')"
          input-type="select"
          :select-options="supportedLocales"
          @update:modelValue="updateLocale"
        />
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
import {WeightUnit} from 'src/api/main/submission';
import {resolveLocale, supportedLocales} from 'src/i18n';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';

const route = useRoute();
const router = useRouter();
const quasar = useQuasar();
const authStore = useAuthStore();
const i18n = useI18n({useScope: 'global'});

const personalDetails: Ref<UserPersonalDetails | undefined> = ref();
const preferences: Ref<UserPreferences | undefined> = ref();

let initialPersonalDetails: UserPersonalDetails | null = null;
let initialPreferences: UserPreferences | null = null;

const newPassword = ref('');

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

  newPassword.value = '';
});

const personalDetailsChanged = computed(() => {
  return initialPersonalDetails !== null &&
    JSON.stringify(initialPersonalDetails) !== JSON.stringify(personalDetails.value);
});

const preferencesChanged = computed(() => {
  return initialPreferences !== null &&
    JSON.stringify(initialPreferences) !== JSON.stringify(preferences.value);
});

const canUpdatePassword = computed(() => {
  const p = newPassword.value;
  return p.length >= 8;
});

async function savePersonalDetails() {
  if (personalDetails.value) {
    try {
      personalDetails.value = await api.auth.updateMyPersonalDetails(authStore, personalDetails.value);
      initialPersonalDetails = structuredClone(toRaw(personalDetails.value));
    } catch (error: any) {
      if (error.response && error.response.status === 400) {
        console.warn('bad request');
      } else {
        console.error(error);
      }
    }
  }
}

function undoPersonalDetailsChanges() {
  personalDetails.value = structuredClone(initialPersonalDetails);
}

async function savePreferences() {
  if (preferences.value) {
    preferences.value = await api.auth.updateMyPreferences(authStore, preferences.value);
    initialPreferences = structuredClone(toRaw(preferences.value));
    updateLocale();
  }
}

function updateLocale() {
  const chosenLocale = resolveLocale(preferences.value?.locale);
  i18n.locale.value = chosenLocale.value;
}

function undoPreferencesChanges() {
  preferences.value = structuredClone(initialPreferences);
  updateLocale();
}

async function updatePassword() {
  try {
    await api.auth.updatePassword(newPassword.value, authStore);
    newPassword.value = '';
    quasar.notify({
      message: i18n.t('userSettingsPage.passwordUpdated'),
      type: 'positive',
      position: 'top'
    });
  } catch (error: any) {
    if (error.response && error.response.status === 400) {
      newPassword.value = '';
      quasar.notify({
        message: i18n.t('userSettingsPage.passwordInvalid'),
        type: 'warning',
        position: 'top'
      });
    } else {
      quasar.notify({
        message: i18n.t('generalErrors.apiError'),
        type: 'danger',
        position: 'top'
      });
    }
  }
}
</script>

<style scoped>
.property-label {
  font-weight: bold;
  margin-top: auto;
  margin-bottom: auto;
}

.save-button-row {
  display: flex;
  flex-direction: row;
  column-gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
  margin-bottom: 10px;
}
</style>
