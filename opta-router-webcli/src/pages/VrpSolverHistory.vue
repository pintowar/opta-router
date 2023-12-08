<script lang="ts" setup>
import { ref, watch, computed } from "vue";
import { useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { VrpSolverRequest, VrpSolverObjective } from "../api";

import { VrpPageLayout, VrpSolverPanelLayout } from "../layout";
import SolutionsHistoryChart from "../components/SolutionsHistoryChart.vue";

const route = useRoute();
const url = ref(`/api/solver-history/${route.params.id}/solutions`);
const { isFetching, error, data: solutions } = useFetch(url).get().json<VrpSolverObjective[]>();

const selectedSolver = ref<string>("all");
const solvers = computed(() => new Set(solutions?.value?.map((it) => it.solver) || []));

const selectedRequest = ref<VrpSolverRequest | null>(null);
const requestsUrl = computed(() => `/api/solver-history/${route.params.id}/requests/${selectedSolver.value}`);
const { data: requests } = useFetch(requestsUrl, { refetch: true }).get().json<VrpSolverRequest[]>();

const filteredSolutions = computed(() => {
  return (
    solutions?.value?.filter(
      (it) =>
        (selectedSolver.value !== "all" ? it.solver === selectedSolver.value : true) &&
        (selectedRequest.value ? it.solverKey === selectedRequest.value.requestKey : true)
    ) || []
  );
});

watch(requests, () => {
  selectedRequest.value = requests?.value?.length ? requests.value[0] : null;
});

function requestStatus(request: VrpSolverRequest | null): string {
  switch (request?.status) {
    case "ENQUEUED":
      return "text-info";
    case "RUNNING":
      return "text-success";
    case "TERMINATED":
      return "text-warning";
    case "NOT_SOLVED":
      return "text-error";
    default:
      return "";
  }
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <vrp-solver-panel-layout>
      <template #menu>
        <div class="space-y-2">
          <div class="flex space-x-2">
            <div class="basis-1/2">
              <h1>Solver History</h1>
            </div>
            <div class="basis-1/2 flex flex-row-reverse">
              <router-link :to="`/solve/${route.params.id}`" class="link link-primary">Solver »</router-link>
            </div>
          </div>
          <div class="flex space-x-2">
            <label class="relative inline-flex items-center mb-4 cursor-pointer">
              <span class="mr-3 text-sm font-medium">Solver</span>
              <select v-model="selectedSolver" class="select select-bordered select-xs">
                <option value="all">all</option>
                <option v-for="solver in solvers" :key="solver" :value="solver">
                  {{ solver }}
                </option>
              </select>
            </label>
          </div>

          <div v-if="requests?.length" class="flex space-x-2">
            <label class="relative inline-flex items-center mb-4 cursor-pointer">
              <span class="mr-3 text-sm font-medium">Request</span>
              <select
                v-model="selectedRequest"
                :class="`select select-bordered select-xs ${requestStatus(selectedRequest)}`"
              >
                <option
                  v-for="request in requests"
                  :key="request.requestKey"
                  :value="request"
                  :class="requestStatus(request)"
                >
                  {{ request.requestKey }}
                </option>
              </select>
            </label>
          </div>
        </div>
      </template>
      <template #main>
        <solutions-history-chart :solutions="filteredSolutions" :request="selectedRequest" :solvers="[...solvers]" />
      </template>
    </vrp-solver-panel-layout>
  </vrp-page-layout>
</template>
