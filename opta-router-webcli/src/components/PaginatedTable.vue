<script lang="ts" generic="T extends { id: number }" setup>
import { ref, toRefs, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { Page } from "../api";

const props = withDefaults(
  defineProps<{
    page: Page<T> | null;
    selected?: T | null;
    isEditing?: boolean;
  }>(),
  {
    selected: null,
    isEditing: false,
  }
);

const { page, isEditing, selected } = toRefs(props);

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
  <div data-testid="paginated-table" class="flex flex-col grow place-content-between overflow-y-hidden space-y-2">
    <div class="overflow-auto">
      <table class="table table-sm table-zebra w-full overflow-hidden">
        <thead>
          <tr>
            <slot name="head"></slot>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in page?.content || []" :key="idx" class="bg-primary-content hover:bg-base-300">
            <slot v-if="isSelectedRow(row)" name="edit" :item="selected"></slot>
            <slot v-else name="show" :idx="idx" :row="row" class="hover:bg-base-300"></slot>
          </tr>
        </tbody>
        <tfoot>
          <slot name="foot"></slot>
        </tfoot>
      </table>
    </div>

    <div class="flex justify-between">
      <div class="flex min-w-fit">
        <select id="page-size" v-model="pageSize" class="select select-sm select-bordered">
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
