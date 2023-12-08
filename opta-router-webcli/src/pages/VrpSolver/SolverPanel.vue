<script lang="ts" setup>
import { toRefs, computed, ref } from "vue";
import { until } from "@vueuse/core";
import { VrpSolution } from "../../api";

import SolverVehicles from "./SolverVehicles.vue";

const props = defineProps<{
  solution: VrpSolution | null;
  solverStatus: string | null;
  wsStatus: string | null;
  selectedSolver: string;
  solvers: string[];
  isDetailedPath: boolean;
}>();

const emit = defineEmits<{
  (e: "onSolve"): void;
  (e: "onTerminate"): void;
  (e: "onClear"): void;
  (e: "update:isDetailedPath", val: boolean): void;
  (e: "update:selectedSolver", val: string): void;
}>();

const { solution, solverStatus, wsStatus, selectedSolver, solvers, isDetailedPath } = toRefs(props);

const editorDetailedPath = computed({
  get: () => isDetailedPath.value,
  set: (val) => emit("update:isDetailedPath", val),
});

const editorSelectedSolver = computed({
  get: () => selectedSolver.value,
  set: (val) => emit("update:selectedSolver", val),
});

const isRunning = computed(() => ["ENQUEUED", "RUNNING"].includes(solverStatus.value || ""));
const isWsConnected = computed(() => wsStatus.value === "OPEN");

const waitingTermination = ref(false);
const waitingClear = ref(false);

async function wrapperTermination() {
  waitingTermination.value = true;
  emit("onTerminate");
  await until(solverStatus).toMatch((v) => v === "TERMINATED");
  waitingTermination.value = false;
}

async function wrapperClear() {
  waitingClear.value = true;
  emit("onClear");
  await until(solverStatus).toMatch((v) => v === "NOT_SOLVED");
  waitingClear.value = false;
}
</script>

<template>
  <div class="space-y-2">
    <div class="flex space-x-2">
      <div class="basis-1/2">
        <h1>Solver</h1>
      </div>
      <div class="basis-1/2 flex flex-row-reverse">
        <router-link :to="`/solver-history/${solution?.problem.id}`" class="link link-primary"
          >Solver History »</router-link
        >
      </div>
    </div>
    <div class="flex flex-row-reverse space-x-2">
      <div class="flex justify-end space-x-2">
        <div>
          <span v-if="solverStatus" class="badge badge-outline">{{ solverStatus }}</span>
        </div>
        <div class="tooltip tooltip-left" :data-tip="`Web Socket ${isWsConnected ? 'connected' : 'disconnected'}`">
          <span :class="`badge ${isWsConnected ? 'badge-success' : 'badge-error'}`">WS</span>
        </div>
      </div>
    </div>
    <div class="flex space-x-2">
      <label class="relative inline-flex items-center mb-4 cursor-pointer">
        <span class="mr-3 text-sm font-medium">Solver</span>
        <select v-model="editorSelectedSolver" :disabled="!isWsConnected" class="select select-bordered select-xs">
          <option v-for="solver in solvers" :key="solver" :value="solver">
            {{ solver }}
          </option>
        </select>
      </label>

      <label class="relative inline-flex items-center mb-4 cursor-pointer">
        <span class="mr-3 text-sm font-medium">Show Detailed Path</span>
        <input v-model="editorDetailedPath" :disabled="!isWsConnected" type="checkbox" class="toggle" />
      </label>
    </div>
    <div class="flex space-x-2">
      <div class="card-actions">
        <button :disabled="!isWsConnected || isRunning" class="btn btn-sm btn-success" @click="$emit('onSolve')">
          Solve<span v-if="isRunning" class="loading loading-bars loading-xs"></span>
        </button>
        <button
          :disabled="!isWsConnected || !isRunning || waitingTermination"
          class="btn btn-sm btn-warning"
          @click="wrapperTermination"
        >
          Terminate<span v-if="waitingTermination" class="loading loading-bars loading-xs"></span>
        </button>
        <button
          :disabled="!isWsConnected || isRunning || waitingClear"
          class="btn btn-sm btn-error"
          @click="wrapperClear"
        >
          Clear<span v-if="waitingClear" class="loading loading-bars loading-xs"></span>
        </button>
      </div>
    </div>
    <div class="flex space-x-2">
      <span>Distance: {{ solution?.totalDistance || 0 }} | Time: {{ solution?.totalTime || 0 }}</span>
    </div>
    <div class="flex space-x-2">
      <solver-vehicles :solution="solution" />
    </div>
  </div>
</template>
