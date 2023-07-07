<script lang="ts" setup>
import { ref, onBeforeMount, watch } from "vue";
import { computedAsync } from "@vueuse/core";
import { useRoute } from "vue-router";

import { getHistorySolverNames, getHistoryRequests, getHistorySolutions, VrpSolverRequest } from "../api";

import VrpSolverPanelLayout from "../layout/VrpSolverPanelLayout.vue";
import SolutionsHistoryChart from "../components/SolutionsHistoryChart.vue";

const route = useRoute();

const selectedSolver = ref<string>("");
const solvers = ref<string[]>([]);

const selectedRequest = ref<VrpSolverRequest | null>(null);
const requests = computedAsync(async () => {
  return selectedSolver.value ? await getHistoryRequests(+route.params.id, selectedSolver.value) : [];
}, []);

const solutions = computedAsync(async () => {
  return selectedRequest.value ? await getHistorySolutions(+route.params.id, selectedRequest.value.requestKey) : [];
}, []);

onBeforeMount(async () => {
  const solverNames = await getHistorySolverNames(+route.params.id);
  solvers.value = solverNames;
  selectedSolver.value = solverNames[0] || "";
});

watch(requests, () => {
  if (requests.value.length) {
    selectedRequest.value = requests.value[0];
  }
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
              <option v-for="solver in solvers" :key="solver" :value="solver">
                {{ solver }}
              </option>
            </select>
          </label>
        </div>

        <div class="flex space-x-2">
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
      <solutions-history-chart :solutions="solutions" :request="selectedRequest" />
    </template>
  </vrp-solver-panel-layout>
</template>
