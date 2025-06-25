<script lang="ts" generic="T extends { id: number }" setup>
import type { StyleValue } from "vue";
import { ref, toRefs, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { Page } from "../api";

const props = withDefaults(
  defineProps<{
    page: Page<T> | null;
    selected?: T | null;
    style?: StyleValue;
    isEditing?: boolean;
  }>(),
  {
    selected: null,
    isEditing: false,
    style: undefined,
  }
);

const { page, isEditing, selected, style } = toRefs(props);

const router = useRouter();
const route = useRoute();

const pageSizes = [5, 10, 25];
const pageSize = ref(route.query.size || 10);
const hoveredLine = ref<number | null>(null);

watch(pageSize, (size) => {
  router.push({
    query: {
      ...route.query,
      size,
    },
  });
});

function isSelectedRow(row: T) {
  return isEditing.value && row.id === selected?.value?.id;
}

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
          <tr>
            <slot name="head"></slot>
          </tr>
        </thead>
        <tbody v-for="(row, idx) in page?.content || []" :key="idx">
          <tr v-if="isSelectedRow(row)" class="bg-primary-content">
            <slot name="edit" :item="selected"></slot>
          </tr>
          <tr
            v-else
            :class="`${idx === hoveredLine ? 'hover' : ''}`"
            @mouseenter="() => (hoveredLine = idx)"
            @mouseleave="() => (hoveredLine = null)"
          >
            <slot name="show" :idx="idx" :row="row"></slot>
          </tr>
        </tbody>
        <tfoot>
          <slot name="foot"></slot>
        </tfoot>
      </table>
    </div>

    <div class="flex justify-between">
      <div class="flex">
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
