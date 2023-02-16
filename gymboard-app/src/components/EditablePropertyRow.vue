<template>
  <div class="row justify-between">
    <span class="property-label">{{ label }}</span>

    <div v-if="typeof modelValue === 'string'">
      <q-input
        :model-value="modelValue"
        @update:modelValue="onValueUpdated"
        :type="inputType"
        dense
      />
    </div>

    <div v-if="typeof modelValue === 'boolean'">
      <q-toggle :model-value="modelValue" @update:modelValue="onValueUpdated"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import {DateTime} from 'luxon';

interface Props {
  label: string;
  inputType?: any;
  modelValue: string | number | boolean | DateTime;
}
defineProps<Props>();
const emits = defineEmits(['update:modelValue']);

function onValueUpdated(newValue: string) {
  emits('update:modelValue', newValue);
}
</script>

<style scoped>
.property-label {
  font-weight: bold;
  margin-top: auto;
  margin-bottom: auto;
}
</style>
