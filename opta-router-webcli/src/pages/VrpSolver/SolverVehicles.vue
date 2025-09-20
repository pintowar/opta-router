<script lang="ts" setup>
import { rgbaString } from "color-map";
import { createRainbow } from "rainbow-color";
import { computed, toRefs } from "vue";

import type { VrpSolution } from "../../api";

const props = defineProps<{
  solution: VrpSolution | null;
}>();

const { solution } = toRefs(props);

const formatter = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

const vehicles = computed(() => solution.value?.problem.vehicles || []);
const routes = computed(() => solution.value?.routes || []);
const capacities = computed(
  () =>
    solution.value?.routes.map((r, idx) => {
      const capacity = vehicles.value[idx]?.capacity;
      return capacity ? (100 * r.totalDemand) / capacity : 0;
    }) || []
);

const colors = computed(() => {
  return createRainbow(Math.max(vehicles.value.length, 9)).map((c) => rgbaString(c));
});
</script>

<template>
  <div class="flex w-full overflow-x-auto">
    <table class="table table-compact table-zebra table-pin-rows table-pin-cols">
      <thead>
        <tr>
          <th>Name</th>
          <th>Distance</th>
          <th>Time</th>
          <th>Full Capacity</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(capacity, idx) in capacities" :key="idx" class="hover:bg-base-300">
          <td :style="{ color: colors[idx] }">{{ vehicles[idx]?.name }}</td>
          <td>{{ routes[idx]?.distance }}</td>
          <td>{{ routes[idx]?.time }}</td>
          <td>
            <div class="tooltip w-full" :data-tip="`${formatter.format(capacity)}%`">
              <progress class="progress progress-primary" :value="capacity" max="100"></progress>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
