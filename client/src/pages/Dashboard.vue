<script lang="ts" setup>
import { ref, onBeforeMount, onBeforeUnmount, watchEffect } from 'vue';

import { Instance, VrpSolution } from "../api";
import { getSessionId, solve, terminate, destroy, detailedPath } from "../api";
import CardEditor from "../components/CardEditor.vue";
import CardMap from "../components/CardMap.vue";

const instance = ref<Instance | null>(null);
const solution = ref<VrpSolution | null>(null);
const isPathDetailed = ref<boolean>(false);

const solverStatus = ref<string>("");
const session = ref<string | null>(null);

const webCli = ref<WebSocket | null>(null);

onBeforeMount(async () => {
  session.value = await getSessionId();

  const cli = new WebSocket(`ws://${location.host}/solution-state/${session.value}`)
  cli.onopen = () => console.info("Connected to the web socket")
  cli.onmessage = (message) => {
    const payload = JSON.parse(message.data);
    solution.value = payload.solution as VrpSolution;
    solverStatus.value = payload.status.status as string;
  };

  webCli.value = cli
});

onBeforeUnmount(() => {
  webCli.value?.close()
});

watchEffect(async () => {
  const status = await detailedPath(isPathDetailed.value)
  // isPathDetailed.value = status.detailedPath
});

async function solveAction() {
  if (instance.value !== null) {
    const { status } = await solve(instance.value)
    solverStatus.value = status;
  }
}

async function terminateAction() {
  const { status } = await terminate()
  solverStatus.value = status;
}

async function destroyAction() {
  const { status } = await destroy()
  solverStatus.value = status;
}

</script>

<template>
  <main class="flex flex-row space-x-4 px-4 py-4">
    <CardEditor 
      v-model:instance="instance"
      v-model:detailed="isPathDetailed"
      :solver-status="solverStatus" 
      extra-class="basis-1/2" 
      @on-solve="solveAction"
      @on-terminate="terminateAction"
      @on-destroy="destroyAction"
    />

    <CardMap :instance="instance" :solution="solution" extra-class="basis-1/2"/>
  </main>
</template>