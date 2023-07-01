<script lang="ts" setup>
import { ref, onBeforeMount, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { useWebSocket } from "@vueuse/core";

import { getSolverNames, detailedPath, getPanelSolutionState, solve, terminate, clean } from "../api";
import { VrpSolution } from "../api";

import SolverMap from "../components/SolverMap.vue";
import SolverPanel from "../components/SolverPanel.vue";

const route = useRoute();
const { status, data } = useWebSocket<string>(`ws://${location.host}/ws/solution-state/${route.params.id}`);

const solverStatus = ref<string | null>(null);

const selectedSolver = ref<string>("");
const solvers = ref<string[]>([]);
const isDetailedPath = ref<boolean>(false);

const solution = ref<VrpSolution | null>(null);
const problem = computed(() => solution.value?.problem);

onBeforeMount(async () => {
  const solverNames = await getSolverNames();
  solvers.value = solverNames;
  selectedSolver.value = solverNames[0] || "";

  const solutionState = await getPanelSolutionState(+route.params.id);
  isDetailedPath.value = solutionState?.solverPanel.isDetailedPath || false;
  solverStatus.value = solutionState?.solutionState.status || null;
  solution.value = solutionState?.solutionState.solution || null;
});

watch(isDetailedPath, async () => {
  if (problem.value) {
    await detailedPath(problem.value?.id, isDetailedPath.value || false);
  }
});

watch(data, () => {
  if (data.value) {
    const payload = JSON.parse(data.value);
    solution.value = payload.solution;
    solverStatus.value = payload.status;
  }
});

async function solveAction() {
  if (problem.value) {
    const state = await solve(problem.value.id, selectedSolver.value);
    solverStatus.value = state || null;
  }
}

async function terminateAction() {
  if (problem.value) {
    const state = await terminate(problem.value.id);
    solverStatus.value = state || null;
  }
}

async function cleanAction() {
  if (problem.value) {
    const state = await clean(problem.value.id);
    solverStatus.value = state || null;
  }
}
</script>

<template>
  <div class="flex my-2 mx-2 space-x-2" style="height: calc(100vh - 140px)">
    <div class="flex-initial w-96">
      <solver-panel
        v-model:is-detailed-path="isDetailedPath"
        v-model:selected-solver="selectedSolver"
        :solution="solution"
        :solvers="solvers"
        :solver-status="solverStatus"
        :ws-status="status"
        @on-solve="solveAction"
        @on-terminate="terminateAction"
        @on-clear="cleanAction"
      />
    </div>
    <div class="flex-auto">
      <solver-map :solution="solution" />
    </div>
  </div>
</template>
