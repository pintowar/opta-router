<script lang="ts" setup>
import { useRoute } from "vue-router";
import { ref, onBeforeUnmount, watch } from "vue";

import { VrpProblem, VrpSolution } from "../api";
import { solve, terminate, clean, detailedPath, getPanelSolutionState } from "../api";
import CardEditor from "../components/CardEditor.vue";
import CardMap from "../components/CardMap.vue";
import CardVehicles from "../components/CardVehicles.vue";

const route = useRoute();

const solutionState = await getPanelSolutionState(+route.params.id);
const solution = ref<VrpSolution | null>(solutionState?.solutionState?.solution || null);
const problem = ref<VrpProblem | null>(solution.value?.problem || null);

const status = ref<string | null>(solutionState?.solutionState?.state || null);
const isDetailedPath = ref<boolean>(solutionState?.solverPanel?.isDetailedPath || false);

const isWsConnected = ref<boolean>(false);
const webCli = creatWSCli();

onBeforeUnmount(() => {
  webCli.close();
});

watch(isDetailedPath, async () => {
  if (problem.value) {
    await detailedPath(problem.value?.id, isDetailedPath.value || false);
  }
});

function creatWSCli() {
  const cli = new WebSocket(`ws://${location.host}/ws/solution-state/${route.params.id}`);
  cli.onopen = () => (isWsConnected.value = true);
  cli.onmessage = (message) => {
    const payload = JSON.parse(message.data);
    solution.value = payload.solution;
    status.value = payload.state;
  };
  cli.onclose = () => (isWsConnected.value = true);

  return cli;
}

async function solveAction() {
  if (problem.value !== null) {
    const state = await solve(problem.value.id);
    status.value = state || null;
  }
}

async function terminateAction() {
  if (problem.value) {
    const state = await terminate(problem.value.id);
    status.value = state || null;
  }
}

async function cleanAction() {
  if (problem.value) {
    const state = await clean(problem.value.id);
    status.value = state || null;
  }
}
</script>

<template>
  <div class="flex flex-col mx-4">
    <div class="grid grid-cols-1 py-2">
      <card-editor
        v-model:is-detailed-path="isDetailedPath"
        :status="status"
        :is-ws-connected="isWsConnected"
        @on-solve="solveAction"
        @on-terminate="terminateAction"
        @on-destroy="cleanAction"
      />
    </div>
    <div class="grid grid-cols-2 gap-4 py-2">
      <card-map :solution="solution" />
      <card-vehicles :solution="solution" />
    </div>
  </div>
</template>
