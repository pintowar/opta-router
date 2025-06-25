<script lang="ts" setup>
import { toRefs, computed } from "vue";
import { uniqBy } from "lodash";
import type { Customer, Depot, VehicleRoute, VrpSolution } from "../../api";

import { LocationMap } from "../../components";

const props = defineProps<{
  solution: VrpSolution | null;
}>();

const { solution } = toRefs(props);

const problem = computed(() => solution.value?.problem);

const depots = computed(() => uniqBy(problem.value?.vehicles?.map((v) => v.depot) || [], "id"));

const locations = computed<(Depot | Customer)[]>(() => {
  return (depots.value || []).concat(problem.value?.customers || []);
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
