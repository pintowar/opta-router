<script lang="ts" setup>
import { ref, toRefs } from "vue";
import { useRoute, useRouter } from "vue-router";

const props = defineProps<{
  query: string;
}>();

const { query } = toRefs(props);
const content = ref<string>(query.value);

const router = useRouter();
const route = useRoute();

function handleKeypress(e: KeyboardEvent) {
  if (e.code === "Enter") {
    search(content.value);
  }
}

function clear() {
  search("");
}

function search(q: string) {
  router.push({
    query: {
      ...route.query,
      q,
    },
  });
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
        @keypress="handleKeypress"
      />
      <div
        class="absolute inset-y-0 end-0 flex items-center pe-3 cursor-pointer"
        data-testid="clear-button"
        @click="clear"
      >
        <v-icon name="md-close" />
      </div>
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
