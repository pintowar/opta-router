<script lang="ts" setup>
import { computed, toRefs } from "vue";
import { useColorMode } from "@vueuse/core";
import { createRainbow } from "rainbow-color";
import { rgbaString } from "color-map";

import { categories } from "../../themes.ts";
import type { VrpSolverRequest, VrpSolverObjective } from "../../api";

const props = defineProps<{
  solutions: VrpSolverObjective[];
  request: VrpSolverRequest | null;
  solvers: string[];
}>();

const { solutions, request } = toRefs(props);

const mode = useColorMode({
  attribute: "data-theme",
});

const themeCategory = computed(() => {
  const category = mode.value === "auto" ? mode.system.value : categories[mode.value];
  return category === "dark" ? "dark" : "light";
});

const colors = createRainbow(Math.max(props.solvers.length, 9))
  .map((c) => rgbaString(c))
  .slice(0, props.solvers.length);

const series = computed(() => {
  const solvers = new Set(solutions?.value?.map((it) => it.solver) || []);
  return [...solvers].map((it) => ({
    name: it,
    data: solutions?.value?.map((sol) => (sol.solver === it ? sol.objective : null)),
  }));
});

const durations = computed(() => {
  const begin = solutions.value.length ? new Date(solutions.value[0].createdAt) : new Date();
  return solutions.value.length
    ? solutions.value.map((it) => (new Date(it.createdAt).getTime() - begin.getTime()) / 1000)
    : [];
});

const bestScore = computed(() =>
  Math.min(...solutions.value.filter((it) => it.objective > 0.0).map((it) => it.objective))
);
const maxDuration = computed(() => Math.max(...durations.value));

const chartOptions = computed(() => {
  const formatter = new Intl.NumberFormat("en-US", { maximumSignificantDigits: 2 });
  const formatedDurations = durations.value.map((it) => formatter.format(it));
  const selectedColor = colors[props.solvers.findIndex((it) => it === request.value?.solver)];

  return {
    chart: {
      height: 350,
      type: "line",
      background: "transparent",
      zoom: {
        enabled: false,
      },
    },
    theme: { mode: themeCategory.value },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      curve: "straight",
    },
    colors: selectedColor ? [selectedColor] : colors,
    title: {
      text: request.value
        ? `${request.value.status} - ${request.value?.requestKey} | ${bestScore.value} m : ${maxDuration.value} s`
        : `${bestScore.value} m : ${maxDuration.value} s`,
      align: "center",
    },
    legend: {
      position: "top",
    },
    grid: {
      row: {
        colors: ["#f3f3f3", "transparent"], // takes an array which will be repeated on columns
        opacity: 0.5,
      },
    },
    xaxis: {
      categories: formatedDurations,
      title: {
        text: "Duration (secs)",
      },
    },
  };
});
</script>

<template>
  <apexchart type="line" height="350" :options="chartOptions" :series="series" />
</template>
