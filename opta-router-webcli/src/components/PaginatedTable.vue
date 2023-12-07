<script lang="ts" generic="T" setup>
import { StyleValue, ref, toRefs, watch } from "vue";
import { Page } from "../api";
import { useRouter, useRoute } from "vue-router";

const props = defineProps<{
  page: Page<T> | null;
  style?: StyleValue;
}>();

const { page } = toRefs(props);

const router = useRouter();
const route = useRoute();

const pageSizes = [5, 10, 25];
const pageSize = ref(route.query.size || 10);

watch(pageSize, (size) => {
  router.push({
    query: {
      ...route.query,
      size,
    },
  });
});

function paginate(page: number) {
  router.push({
    query: {
      ...route.query,
      page,
    },
  });
}

function first() {
  paginate(0);
}

function prev() {
  paginate(Math.max((page?.value?.number || 0) - 1, 0));
}

function next() {
  paginate((page?.value?.number || 0) + 1);
}

function last() {
  paginate(Math.max((page?.value?.totalPages || 0) - 1, 0));
}
</script>

<template>
  <div class="flex-col space-y-2">
    <div class="overflow-y-auto overflow-x-hidden" :style="style">
      <table class="table table-sm table-zebra w-full">
        <thead>
          <slot name="head"></slot>
        </thead>
        <tbody>
          <slot v-for="(row, idx) in page?.content || []" :key="idx" :idx="idx" :row="row" name="body"></slot>
        </tbody>
        <tfoot>
          <slot name="foot"></slot>
        </tfoot>
      </table>
    </div>

    <div class="flex justify-around">
      <div>
        <select v-model="pageSize" class="select select-sm select-bordered">
          <option v-for="size in pageSizes" :key="size" :value="size">
            {{ size }}
          </option>
        </select>
      </div>

      <div class="join justify-center w-full">
        <button :class="`join-item btn btn-sm ${page?.first ? 'btn-disabled' : ''}`" @click="first">«</button>
        <button :class="`join-item btn btn-sm ${page?.first ? 'btn-disabled' : ''}`" @click="prev">‹</button>
        <button class="join-item btn btn-sm">Page {{ (page?.number || 0) + 1 }}</button>
        <button :class="`join-item btn btn-sm ${page?.last ? 'btn-disabled' : ''}`" @click="next">›</button>
        <button :class="`join-item btn btn-sm ${page?.last ? 'btn-disabled' : ''}`" @click="last">»</button>
      </div>

      <div>
        <p>&nbsp;</p>
      </div>
    </div>
  </div>
</template>
