<script setup lang="ts">
import type { AfterFetchContext } from "@vueuse/core";
import { useFetch } from "@vueuse/core";
import { computed, toRefs } from "vue";
import { useRoute } from "vue-router";
import type { VrpProblem } from "../../api";
import { VrpPageLayout } from "../../layout";
import VrpProblemForm from "./VrpProblemForm/VrpProblemForm.vue";

const props = defineProps<{
  mode: "copy" | "create" | "update";
}>();

const { mode } = toRefs(props);

const route = useRoute();

const defaultProblem: VrpProblem = {
  id: -1,
  name: "",
  vehicles: [],
  customers: [],
};

const problemUrl = computed(() => `/api/vrp-problems/${route.params.id}`);
const persistUrl = computed(() =>
  mode.value !== "create" ? `${problemUrl.value}/${mode.value}` : "/api/vrp-problems"
);

const { isFetching, error, data } = useFetch(problemUrl, {
  initialData: defaultProblem,
  immediate: mode.value !== "create",
  afterFetch: afterProblemFetch,
})
  .get()
  .json<VrpProblem>();

function afterProblemFetch(ctx: AfterFetchContext<VrpProblem>) {
  ctx.data = {
    id: ctx.data?.id || defaultProblem.id,
    name: ctx.data?.name || defaultProblem.name,
    vehicles: ctx.data?.vehicles || defaultProblem.vehicles,
    customers: ctx.data?.customers || defaultProblem.customers,
  };
  return ctx;
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <vrp-problem-form v-if="data" v-model:problem="data" :persist-url="persistUrl" />
    <vrp-problem-form v-else :problem="defaultProblem" :persist-url="persistUrl" />
  </vrp-page-layout>
</template>
