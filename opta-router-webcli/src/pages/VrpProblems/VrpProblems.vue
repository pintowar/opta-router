<script lang="ts" setup>
import { computed, ref } from "vue";
import { useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Page, VrpProblemSummary } from "../../api";

import { VrpPageLayout } from "../../layout";
import { AlertMessage, DeleteDialog, InputSearch, PaginatedTable } from "../../components";

const route = useRoute();

const url = computed(
  () => `/api/vrp-problems?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ""}`
);
const {
  isFetching,
  error,
  data: page,
  execute: fetchProblems,
} = useFetch(url, { refetch: true }).get().json<Page<VrpProblemSummary>>();

const selectedProblem = ref<VrpProblemSummary | null>(null);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `/api/vrp-problems/${selectedProblem.value?.id}/remove`);
const removeError = ref(false);

const showDeleteModal = (instance: VrpProblemSummary) => {
  selectedProblem.value = instance;
  openRemove.value = true;
};
</script>

<template>
  <vrp-page-layout v-slot="{ tableFooterHeight }" :is-fetching="isFetching" :error="error">
    <main>
      <div class="mx-2 my-2 space-x-2">
        <alert-message
          v-if="removeError"
          :message="`Could not remove VrpProblem: ${selectedProblem?.name}`"
          variant="error"
        />

        <delete-dialog
          v-model:url="removeUrl"
          v-model:open="openRemove"
          :message="`Are you sure you want to delete ${selectedProblem?.name} (id: ${selectedProblem?.id})?`"
          @success-remove="fetchProblems"
          @fail-remove="removeError = true"
        />

        <h1 class="text-2xl">Routes</h1>
        <div class="flex w-full justify-between pr-2">
          <input-search :query="`${route.query.q || ''}`" />
          <router-link to="/problem/new" class="btn btn-circle">
            <v-icon name="md-add" />
          </router-link>
        </div>

        <paginated-table :page="page" :style="`height: calc(100vh - ${tableFooterHeight})`">
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
                <button
                  :disabled="row.numSolverRequests > 0"
                  class="btn btn-sm btn-circle"
                  @click="showDeleteModal(row)"
                >
                  <v-icon name="md-deleteoutline" />
                </button>
              </div>
            </td>
          </template>
        </paginated-table>
      </div>
    </main>
  </vrp-page-layout>
</template>
