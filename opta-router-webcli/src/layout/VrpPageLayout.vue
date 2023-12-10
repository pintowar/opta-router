<script lang="ts" setup>
import { AlertMessage } from "../components";

const props = defineProps<{
  isFetching: boolean | null;
  error: unknown | null;
}>();
</script>
<template>
  <main v-if="props.isFetching" class="flex items-center justify-center h-full">
    <div class="mt-32">
      <button class="btn btn-ghost loading">Loading</button>
    </div>
  </main>
  <main v-else-if="props.error" class="flex items-center justify-center h-full">
    <alert-message message="Error! Failed to load data" variant="error" />
  </main>
  <slot v-else :table-footer-height="'270px'" :map-footer-height="'155px'"></slot>
</template>
