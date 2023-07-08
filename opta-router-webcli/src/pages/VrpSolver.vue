<script lang="ts" setup>
import { ref, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { useFetch, useWebSocket, watchOnce } from "@vueuse/core";

import { PanelSolutionState, VrpProblem, VrpSolution } from "../api";

import VrpSolverPanelLayout from "../layout/VrpSolverPanelLayout.vue";
import SolverMap from "../components/SolverMap.vue";
import SolverPanel from "../components/SolverPanel.vue";

const route = useRoute();

const solverStatus = ref<string | null>(null);
const solution = ref<VrpSolution | null>(null);
const problem = ref<VrpProblem | null>(null);
const isDetailedPath = ref<boolean>(false);
const selectedSolver = ref<string>("");

const webSocketUrl = computed(() => `ws://${location.host}/ws/solution-state/${route.params.id}`);
const solverNamesUrl = ref("/api/solver/solver-names");
const solutionPanelUrl = computed(() => `/api/solver/${route.params.id}/solution-panel`);
const detailedPathUrl = computed(() => `/api/solver/${route.params.id}/detailed-path/${isDetailedPath.value}`);
const solveUrl = computed(() => `/api/solver/${route.params.id}/solve/${selectedSolver.value}`);
const terminateUrl = computed(() => `/api/solver/${route.params.id}/terminate`);
const cleanUrl = computed(() => `/api/solver/${route.params.id}/clean`);

const { status, data: wsData } = useWebSocket<string>(webSocketUrl);
const { isFetching, error, data: solutionPanel } = useFetch(solutionPanelUrl).get().json<PanelSolutionState>();
const { data: solvers } = useFetch(solverNamesUrl, { initialData: [] }).get().json<string[]>();
const { execute: detailedPath } = useFetch(detailedPathUrl, { immediate: false }).put();
const { data: solveStatus, execute: solve } = useFetch(solveUrl, { immediate: false }).post().json<string>();
const { data: termStatus, execute: terminate } = useFetch(terminateUrl, { immediate: false }).post().json<string>();
const { data: cleanStatus, execute: clean } = useFetch(cleanUrl, { immediate: false }).post().json<string>();

watchOnce(solutionPanel, () => {
  isDetailedPath.value = solutionPanel.value?.solverPanel.isDetailedPath || false;
  solverStatus.value = solutionPanel.value?.solutionState.status || null;
  solution.value = solutionPanel.value?.solutionState.solution || null;
  problem.value = solution.value?.problem || null;
});

watchOnce(solvers, () => {
  selectedSolver.value = (solvers.value && solvers.value[0]) || "";
});

watch(isDetailedPath, async () => {
  if (problem.value) {
    await detailedPath();
  }
});

watch(wsData, () => {
  if (wsData.value) {
    const payload = JSON.parse(wsData.value);
    solution.value = payload.solution;
    solverStatus.value = payload.status;
  }
});

async function solveAction() {
  if (problem.value) {
    await solve();
    solverStatus.value = solveStatus.value || null;
  }
}

async function terminateAction() {
  if (problem.value) {
    await terminate();
    solverStatus.value = termStatus.value || null;
  }
}

async function cleanAction() {
  if (problem.value) {
    await clean();
    solverStatus.value = cleanStatus.value || null;
  }
}
</script>

<template>
  <main v-if="isFetching" class="flex items-center justify-center h-full">
    <div class="mt-32">
      <button class="btn btn-ghost loading">Loading</button>
    </div>
  </main>
  <main v-else-if="error" class="flex items-center justify-center h-full">
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
  <vrp-solver-panel-layout v-else>
    <template #menu>
      <solver-panel
        v-model:is-detailed-path="isDetailedPath"
        v-model:selected-solver="selectedSolver"
        :solution="solution"
        :solvers="solvers || []"
        :solver-status="solverStatus"
        :ws-status="status"
        @on-solve="solveAction"
        @on-terminate="terminateAction"
        @on-clear="cleanAction"
      />
    </template>
    <template #main>
      <solver-map :solution="solution" />
    </template>
  </vrp-solver-panel-layout>
</template>
