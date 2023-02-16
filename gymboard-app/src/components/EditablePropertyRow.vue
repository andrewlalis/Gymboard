<template>
  <div class="row justify-between">
    <span class="property-label">{{ label }}</span>

    <div v-if="(typeof modelValue === 'string' || typeof modelValue === 'number') && inputType !== 'select'">
      <q-input
        :model-value="modelValue"
        @update:modelValue="onValueUpdated"
        :type="inputType"
        :step="numberInputStep"
        dense
      />
    </div>

    <div v-if="typeof modelValue === 'boolean'">
      <q-toggle :model-value="modelValue" @update:modelValue="onValueUpdated"/>
    </div>

    <div v-if="inputType === 'select'">
      <q-select
        :model-value="modelValue"
        :options="selectOptions"
        emit-value
        map-options
        @update:modelValue="onValueUpdated"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {DateTime} from 'luxon';

interface Props {
  label: string;
  inputType?: any;
  modelValue: string | number | boolean | DateTime;
  selectOptions?: Array<object>;
  numberInputStep?: number;
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
