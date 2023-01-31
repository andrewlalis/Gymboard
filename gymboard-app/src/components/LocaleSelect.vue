<template>
  <q-select
    v-model="i18n.locale.value"
    :options="localeOptions"
    :label="$t('mainLayout.language')"
    dense
    borderless
    emit-value
    map-options
    options-dense
    filled
    hide-bottom-space
    dark
    options-dark
    label-color="white"
    options-selected-class="text-grey"
    style="min-width: 150px"
  />
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const i18n = useI18n({ useScope: 'global' });
const localeOptions = [
  { value: 'en-US', label: 'English' },
  { value: 'nl-NL', label: 'Nederlands' },
  { value: 'de', label: 'Deutsch' }
];
</script>

<style scoped></style>
