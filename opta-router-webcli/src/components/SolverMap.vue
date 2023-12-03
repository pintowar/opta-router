<script lang="ts" setup>
import { Customer, Depot, VehicleRoute } from "../api";

import LocationMap from "./LocationMap.vue";

import { toRefs, computed } from "vue";

import { VrpSolution } from "../api";

const props = defineProps<{
  solution: VrpSolution | null;
}>();

const { solution } = toRefs(props);

const problem = computed(() => solution.value?.problem);

const locations = computed<(Depot | Customer)[]>(() => {
  return (problem.value?.depots || []).concat(problem.value?.customers || []);
});

const routes = computed<VehicleRoute[]>(() =>
  (solution.value?.routes || []).map((route, idx) => {
    const vehicle = problem.value?.vehicles[idx];
    return { vehicle, route };
  })
);
</script>

<template>
  <location-map :locations="locations" :routes="routes" />
</template>
