<script lang="ts" setup>
import { useRoute } from 'vue-router'
import { ref, onBeforeUnmount, watchEffect } from 'vue';

import { Instance, SolverState, VrpSolution } from "../api";
import { solve, terminate, destroy, detailedPath, getInstance, getSolutionState } from "../api";
import CardEditor from "../components/CardEditor.vue";
import CardMap from "../components/CardMap.vue";

const route = useRoute();

const instance = ref<Instance | null>(await getInstance(+route.params.id));

const solutionState = await getSolutionState(+route.params.id);
const solverState = ref<SolverState | null>(solutionState?.state || null);
const solution = ref<VrpSolution | null>(solutionState?.solution || null);

const isWsConnected = ref<boolean>(false);
const webCli = creatWSCli();

onBeforeUnmount(() => {
  webCli.close()
});

watchEffect(async () => {
  if (instance.value) {
    const state = await detailedPath(instance.value?.id, solverState.value?.detailedPath || false)
    if (solverState.value)
      solverState.value.detailedPath = state?.detailedPath || false;
    }
});

function creatWSCli() {
  const cli = new WebSocket(`ws://${location.host}/ws/solution-state/${route.params.id}`)
  cli.onopen = () => isWsConnected.value = true
  cli.onmessage = (message) => {
    const payload = JSON.parse(message.data);
    solution.value = payload.solution;
    solverState.value = payload.state;
  };
  cli.onclose = () => isWsConnected.value = true

  return cli;
}

async function solveAction() {
  if (instance.value !== null) {
    const state = await solve(instance.value)
    if (solverState.value) {
      solverState.value = state;
    } else {
      solverState.value = { status: "", detailedPath: false }
    }
  }
}

async function terminateAction() {
  if (instance.value) {
    const state = await terminate(instance.value.id)
    if (solverState.value) {
      solverState.value = state;
    } else {
      solverState.value = { status: "", detailedPath: false }
    }
  }
}

async function destroyAction() {
  if (instance.value) {
    const state = await destroy(instance.value.id)
    if (solverState.value) {
      solverState.value = state;
    } else {
      solverState.value = { status: "", detailedPath: false }
    }
  }
}
</script>

<template>
  <div class="grid grid-cols-2 gap-4 px-4 py-4">
    <CardEditor 
      v-model:instance="instance"
      v-model:solver-state="solverState"
      :is-ws-connected="isWsConnected"
      
      extra-class="" 
      @on-solve="solveAction"
      @on-terminate="terminateAction"
      @on-destroy="destroyAction"
    />

    <CardMap :instance="instance" :solution="solution" extra-class=""/>
  </div>
</template>