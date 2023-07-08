<script lang="ts" setup>
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
      <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <span>Error! Failed to load data</span>
    </div>
  </main>
  <slot v-else></slot>
</template>
