<script lang="ts" setup>
import { useRoute } from "vue-router";
import { ref, onBeforeUnmount, watch } from "vue";

import { Instance, VrpSolution } from "../api";
import { solve, terminate, clean, detailedPath, getInstance, getSolutionState } from "../api";
import CardEditor from "../components/CardEditor.vue";
import CardMap from "../components/CardMap.vue";

const route = useRoute();

const instance = ref<Instance | null>(await getInstance(+route.params.id));

const solutionState = await getSolutionState(+route.params.id);
const solution = ref<VrpSolution | null>(solutionState?.solution || null);
const status = ref<string | null>(solutionState?.state?.status || null);
const isDetailedPath = ref<boolean>(solutionState?.state?.detailedPath || false);

const isWsConnected = ref<boolean>(false);
const webCli = creatWSCli();

onBeforeUnmount(() => {
  webCli.close();
});

watch(isDetailedPath, async () => {
  if (instance.value) {
    const state = await detailedPath(instance.value?.id, isDetailedPath.value || false);
    isDetailedPath.value = state?.detailedPath || false;
  }
});

function creatWSCli() {
  const cli = new WebSocket(`ws://${location.host}/ws/solution-state/${route.params.id}`);
  cli.onopen = () => (isWsConnected.value = true);
  cli.onmessage = (message) => {
    const payload = JSON.parse(message.data);
    solution.value = payload.solution;
    status.value = payload.state?.status;
  };
  cli.onclose = () => (isWsConnected.value = true);

  return cli;
}

async function solveAction() {
  if (instance.value !== null) {
    const state = await solve(instance.value);
    status.value = state?.status || null;
    isDetailedPath.value = state?.detailedPath || false;
  }
}

async function terminateAction() {
  if (instance.value) {
    const state = await terminate(instance.value.id);
    status.value = state?.status || null;
    isDetailedPath.value = state?.detailedPath || false;
  }
}

async function cleanAction() {
  if (instance.value) {
    const state = await clean(instance.value.id);
    status.value = state?.status || null;
    isDetailedPath.value = state?.detailedPath || false;
  }
}
</script>

<template>
  <div class="grid grid-cols-2 gap-4 px-4 py-4">
    <card-editor
      v-model:instance="instance"
      v-model:is-detailed-path="isDetailedPath"
      :status="status"
      :is-ws-connected="isWsConnected"
      @on-solve="solveAction"
      @on-terminate="terminateAction"
      @on-destroy="cleanAction"
    />

    <card-map :instance="instance" :solution="solution" />
  </div>
</template>
