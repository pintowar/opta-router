<script lang="ts" setup>
import { createRainbow } from "rainbow-color";
import { rgbaString } from "color-map";
import { toRefs, computed } from "vue";

import { VrpSolution } from "../api";

const props = defineProps<{
  solution: VrpSolution | null;
}>();

const { solution } = toRefs(props);

const formatter = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

const vehicles = computed(() => solution.value?.problem.vehicles || []);
const routes = computed(() => solution.value?.routes || []);
const capacities = computed(
  () => solution.value?.routes.map((r, idx) => (100 * r.totalDemand) / vehicles.value[idx].capacity) || []
);

const colors = computed(() => {
  return createRainbow(Math.max(vehicles.value.length, 9)).map((c) => rgbaString(c));
});
</script>

<template>
  <div class="overflow-y-auto">
    <table class="table table-compact table-zebra w-full">
      <thead>
        <th>Name</th>
        <th>Distance</th>
        <th>Time</th>
        <th>Full Capacity</th>
      </thead>
      <tbody v-for="(capacity, idx) in capacities" :key="idx">
        <td :style="{ color: colors[idx] }">{{ vehicles[idx].name }}</td>
        <td>{{ routes[idx].distance }}</td>
        <td>{{ routes[idx].time }}</td>
        <td>
          <div class="tooltip w-full" :data-tip="`${formatter.format(capacity)}%`">
            <progress class="progress progress-primary" :value="capacity" max="100"></progress>
          </div>
        </td>
      </tbody>
    </table>
  </div>
</template>
