<script lang="ts" setup>
import { toRefs, computed } from "vue";
import { VrpSolution } from "../api";

import SolverVehicles from "../components/SolverVehicles.vue";

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

const isWsConnected = computed(() => wsStatus.value === "OPEN");
const badgeColor = computed(() => `badge-${isWsConnected.value ? "success" : "error"}`);
</script>

<template>
  <div class="space-y-2">
    <div class="flex space-x-2">
      <div class="basis-1/2">
        <h1>Solver</h1>
      </div>
      <div class="basis-1/2 flex flex-row-reverse">
        <router-link :to="`/solver-history/${solution?.problem.id}`" class="link link-primary">History »</router-link>
      </div>
    </div>
    <div class="flex flex-row-reverse space-x-2">
      <div class="flex justify-end space-x-2">
        <div>
          <span v-if="solverStatus" class="badge badge-outline">{{ solverStatus }}</span>
        </div>
        <div class="tooltip" :data-tip="`Web Socket ${isWsConnected ? 'connected' : 'disconnected'}`">
          <div :class="`badge ${badgeColor}`">WS</div>
        </div>
      </div>
    </div>
    <div class="flex space-x-2">
      <label class="relative inline-flex items-center mb-4 cursor-pointer">
        <span class="mr-3 text-sm font-medium">Solver</span>
        <select v-model="editorSelectedSolver" class="select select-bordered select-xs">
          <option v-for="solver in solvers" :key="solver" :value="solver">
            {{ solver }}
          </option>
        </select>
      </label>

      <label class="relative inline-flex items-center mb-4 cursor-pointer">
        <span class="mr-3 text-sm font-medium">Show Detailed Path</span>
        <input v-model="editorDetailedPath" type="checkbox" class="toggle" />
      </label>
    </div>
    <div class="flex space-x-2">
      <div class="card-actions">
        <button class="btn btn-sm btn-success" @click="$emit('onSolve')">Solve</button>
        <button class="btn btn-sm btn-warning" @click="$emit('onTerminate')">Terminate</button>
        <button class="btn btn-sm btn-error" @click="$emit('onClear')">Clear</button>
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
