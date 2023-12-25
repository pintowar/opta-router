<script setup lang="ts">
import { computed, ref, toRefs } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";
import { VrpPageLayout } from "../../layout";
import { EditableVrpProblem } from "../../api";
import VrpProblemForm from "./VrpProblemForm/VrpProblemForm.vue";

const props = defineProps<{
  mode: "copy" | "create" | "update";
}>();

const { mode } = toRefs(props);

const route = useRoute();

const defaultProblem: EditableVrpProblem = {
  id: -1,
  name: "",
  vehicles: [],
  customers: [],
}

const problemUrl = computed(() => `/api/vrp-problems/${route.params.id}`);
const persistUrl = computed(() => mode.value !== "create" ? `${problemUrl.value}/${mode.value}` : "/api/vrp-problems");

const {
  isFetching,
  error,
  data,
} = useFetch(problemUrl, { initialData: defaultProblem, immediate: mode.value !== "create", afterFetch: afterProblemFetch }).get().json<EditableVrpProblem>();

function afterProblemFetch(ctx: AfterFetchContext<EditableVrpProblem>) {
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
  <vrp-page-layout v-slot="{ mapFooterHeight }" :is-fetching="isFetching" :error="error">
    <main>
      <vrp-problem-form
        v-if="data"
        :persist-url="persistUrl" 
        v-model:problem="data"
        :style="`height: calc(100vh - ${mapFooterHeight})`"
      />
      <vrp-problem-form
        v-else
        :persist-url="persistUrl" 
        :problem="defaultProblem"
        :style="`height: calc(100vh - ${mapFooterHeight})`"
      />
    </main>
  </vrp-page-layout>
</template>
