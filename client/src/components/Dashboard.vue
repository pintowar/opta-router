<script lang="ts" setup>
import { useRoute } from 'vue-router'
import { ref, onBeforeUnmount, watchEffect } from 'vue';

import { Instance, VrpSolution } from "../api";
import { solve, terminate, destroy, detailedPath, getInstance } from "../api";
import CardEditor from "../components/CardEditor.vue";
import CardMap from "../components/CardMap.vue";

const route = useRoute();
const webCli = creatWSCli();

const instance = ref<Instance | null>(await getInstance(+route.params.id));
const solution = ref<VrpSolution | null>(null);
const isPathDetailed = ref<boolean>(false);

const solverStatus = ref<string>("");

onBeforeUnmount(() => {
  webCli.close()
});

watchEffect(async () => {
  if (instance.value) {
    await detailedPath(instance.value?.id, isPathDetailed.value)
  }
});

function creatWSCli() {
  const cli = new WebSocket(`ws://${location.host}/ws/solution-state/${route.params.id}`)
  cli.onopen = () => console.info("Connected to the web socket")
  cli.onmessage = (message) => {
    const payload = JSON.parse(message.data);
    solution.value = payload.solution as VrpSolution;
    solverStatus.value = payload.state.status as string;
  };
  cli.onclose = () => console.info("Disconnected from the web socket")

  return cli;
}

async function solveAction() {
  if (instance.value !== null) {
    const { status } = await solve(instance.value)
    solverStatus.value = status;
  }
}

async function terminateAction() {
  if (instance.value) {
    const { status } = await terminate(instance.value.id)
    solverStatus.value = status;
  }
}

async function destroyAction() {
  if (instance.value) {
    const { status } = await destroy(instance.value.id)
    solverStatus.value = status;
  }
}

</script>

<template>
  <div class="grid grid-cols-2 gap-4 px-4 py-4">
    <CardEditor 
      v-model:instance="instance"
      v-model:detailed="isPathDetailed"
      :solver-status="solverStatus" 
      extra-class="" 
      @on-solve="solveAction"
      @on-terminate="terminateAction"
      @on-destroy="destroyAction"
    />

    <CardMap :instance="instance" :solution="solution" extra-class=""/>
  </div>
</template>