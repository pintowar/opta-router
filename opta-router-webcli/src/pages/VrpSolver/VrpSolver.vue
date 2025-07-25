<script lang="ts" setup>
import type { AfterFetchContext } from "@vueuse/core";
import { useFetch, useWebSocket, watchOnce } from "@vueuse/core";
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";

import type { PanelSolutionState, VrpProblem, VrpSolution } from "../../api";

import { VrpPageLayout, VrpSolverPanelLayout } from "../../layout";
import SolverMap from "./SolverMap.vue";
import SolverPanel from "./SolverPanel.vue";

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

const { status, data: wsData, open: wsOpen } = useWebSocket<string>(webSocketUrl, { immediate: false });
const {
  isFetching,
  error,
  data: solutionPanel,
} = useFetch(solutionPanelUrl, { afterFetch: afterPanelFetch }).get().json<PanelSolutionState>();
const { data: solvers, execute: fetchSolvers } = useFetch(solverNamesUrl, { initialData: [], immediate: false })
  .get()
  .json<string[]>();
const { execute: detailedPath } = useFetch(detailedPathUrl, { immediate: false }).put();
const { data: solveStatus, execute: solve } = useFetch(solveUrl, { immediate: false }).post().json<string>();
const { execute: terminate } = useFetch(terminateUrl, { immediate: false }).post().json<string>();
const { execute: clean } = useFetch(cleanUrl, { immediate: false }).post().json<string>();

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

function afterPanelFetch(ctx: AfterFetchContext) {
  fetchSolvers();
  wsOpen();
  return ctx;
}

async function solveAction() {
  if (problem.value) {
    await solve();
    solverStatus.value = solveStatus.value || null;
  }
}

async function terminateAction() {
  if (problem.value) {
    await terminate();
  }
}

async function cleanAction() {
  if (problem.value) {
    await clean();
  }
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <vrp-solver-panel-layout>
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
  </vrp-page-layout>
</template>
