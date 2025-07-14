<script lang="ts" setup>
import { useFetch } from "@vueuse/core";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

import type { Page, VrpProblemSummary } from "../../api";

import { PaginatedTable } from "../../components";
import { VrpCrudPageLayout } from "../../layout";

const route = useRoute();
const router = useRouter();

const url = computed(
  () => `/api/vrp-problems?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ""}`
);
const {
  isFetching,
  error,
  data: page,
  execute: fetch,
} = useFetch(url, { refetch: true }).get().json<Page<VrpProblemSummary>>();

const selected = ref<VrpProblemSummary | null>(null);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `/api/vrp-problems/${selected.value?.id}/remove`);
const removeError = ref(false);

const showDeleteModal = (instance: VrpProblemSummary) => {
  selected.value = instance;
  openRemove.value = true;
};

const toogleInsert = () => {
  router.push("/problem/new");
};
</script>

<template>
  <vrp-crud-page-layout
    :is-fetching="isFetching"
    :error="error"
    :remove-error="removeError"
    :update-error="null"
    :insert-error="null"
    :success-update="false"
    :success-insert="false"
    :remove-url="removeUrl"
    :open-remove="openRemove"
    :selected="selected"
    :open-insert="false"
    title="Routes"
    @fetch="fetch"
    @fail-remove="removeError = true"
    @toogle-insert="toogleInsert"
    @update:open-remove="openRemove = $event"
  >
    <paginated-table :page="page">
      <template #head>
        <th>Id</th>
        <th>Name</th>
        <th>Num Locations</th>
        <th>Num Vehicles</th>
        <th>Total Demand</th>
        <th>Total Capacity</th>
        <th>Num Solver Requests</th>
        <th>Solver Request Status</th>
        <th>Actions</th>
      </template>
      <template #show="{ row }">
        <td>{{ row.id }}</td>
        <td>{{ row.name }}</td>
        <td>{{ row.nlocations }}</td>
        <td>{{ row.nvehicles }}</td>
        <td>{{ row.totalDemand }}</td>
        <td>{{ row.totalCapacity }}</td>
        <td>{{ row.numSolverRequests }}</td>
        <td class="space-x-2">
          <div v-if="row.numEnqueuedRequests > 0" class="badge badge-warning tooltip" data-tip="Enqueued">E</div>
          <div v-else-if="row.numRunningRequests > 0" class="badge badge-success tooltip" data-tip="Running">R</div>
          <div v-else class="badge badge-info tooltip" data-tip="Not Solving">N</div>
        </td>
        <td class="space-x-2">
          <div class="tooltip" data-tip="Solve it">
            <router-link :to="`/solve/${row.id}`" class="btn btn-sm btn-circle">
              <v-icon name="oi-gear" />
            </router-link>
          </div>
          <div v-if="row.numSolverRequests === 0" class="tooltip" data-tip="Edit">
            <router-link :to="`/problem/${row.id}/edit`" class="btn btn-sm btn-circle">
              <v-icon name="md-edit-twotone" />
            </router-link>
          </div>
          <div v-else class="tooltip" data-tip="Copy">
            <router-link :to="`/problem/${row.id}/copy`" class="btn btn-sm btn-circle">
              <v-icon name="md-contentcopy" />
            </router-link>
          </div>
          <div class="tooltip" data-tip="Delete">
            <button :disabled="row.numSolverRequests > 0" class="btn btn-sm btn-circle" @click="showDeleteModal(row)">
              <v-icon name="md-deleteoutline" />
            </button>
          </div>
        </td>
      </template>
    </paginated-table>
  </vrp-crud-page-layout>
</template>
