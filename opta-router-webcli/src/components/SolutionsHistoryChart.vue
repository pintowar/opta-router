<script lang="ts" setup>
import { computed, toRefs } from "vue";
import { VrpSolverRequest, VrpSolverObjective } from "../api";

const props = defineProps<{
  solutions: VrpSolverObjective[];
  request: VrpSolverRequest | null;
}>();

const { solutions, request } = toRefs(props);

const series = computed(() => [
  {
    name: "Distance",
    data: solutions.value.map((it) => it.objective),
  },
]);

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
  return {
    chart: {
      height: 350,
      type: "line",
      zoom: {
        enabled: false,
      },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      curve: "straight",
    },
    title: {
      text: request.value
        ? `${request.value.status} - ${request.value?.requestKey} | ${bestScore.value} m : ${maxDuration.value} s`
        : "",
      align: "left",
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
