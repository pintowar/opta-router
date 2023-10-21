<script lang="ts" setup>
import Icon from "../components/Icon.vue";

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
    <div class="mx-32 mt-16 alert alert-error">
      <Icon name="error" />
      <span>Error! Failed to load data</span>
    </div>
  </main>
  <slot v-else></slot>
</template>
