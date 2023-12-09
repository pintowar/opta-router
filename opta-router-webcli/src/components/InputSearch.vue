<script lang="ts" setup>
import { ref, toRefs } from "vue";
import { useRouter, useRoute } from "vue-router";

const props = defineProps<{
  query: string;
}>();

const { query } = toRefs(props);
const content = ref<string>(query.value);

const router = useRouter();
const route = useRoute();

function search(e: KeyboardEvent) {
  if (e.code === "Enter") {
    router.push({
      query: {
        ...route.query,
        q: content.value,
      },
    });
  }
}
</script>

<template>
  <div class="flex items-center">
    <div class="relative w-full">
      <div class="absolute inset-y-0 start-0 flex items-center ps-3 pointer-events-none">
        <v-icon name="md-search" />
      </div>
      <input
        v-model="content"
        class="input input-bordered block w-full p-2.5 ps-10"
        placeholder="Search..."
        @keypress="search"
      />
    </div>
  </div>
</template>

<style>
.input,
input.input {
  height: 2rem;
  padding-right: 0.75rem;
  font-size: 0.875rem;
  line-height: 2rem;
}
</style>
