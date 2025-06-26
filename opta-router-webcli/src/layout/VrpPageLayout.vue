<script lang="ts" setup>
import { AlertMessage } from "../components";

const { isFetching, error } = defineProps<{
  isFetching: boolean | null;
  error: unknown | null;
}>();
</script>
<template>
  <div v-if="isFetching" class="flex items-center justify-center w-full">
    <button class="btn btn-ghost loading">Loading</button>
  </div>
  <div v-else-if="error" class="flex pt-4 justify-center w-full">
    <alert-message message="Error! Failed to load data" variant="error" />
  </div>
  <slot v-else></slot>
</template>
