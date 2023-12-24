<script setup lang="ts">
import { computed } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";
import { uniqBy, sortBy } from "lodash";
import { VrpPageLayout } from "../../layout";
import { Customer, Vehicle } from "../../api";
import { LocationMap } from "../../components";
import VrpDepotTab from "./VrpDepotTab.vue";
import VrpCustomersTab from "./VrpCustomersTab.vue";

type EditableProblem = {
  id: number;
  name: string;
  vehicles: Vehicle[];
  customers: Customer[];
};

const route = useRoute();

const problemUrl = computed(() => `/api/vrp-problems/${route.params.id}`);
const updateUrl = computed(() => `${problemUrl.value}/update`);
const {
  isFetching,
  error,
  data: problem,
} = useFetch(problemUrl, { afterFetch: afterProblemFetch }).get().json<EditableProblem>();

function afterProblemFetch(ctx: AfterFetchContext<EditableProblem>) {
  ctx.data = {
    id: ctx.data?.id || -1,
    name: ctx.data?.name || "",
    vehicles: ctx.data?.vehicles || [],
    customers: ctx.data?.customers || [],
  };
  return ctx;
}

const { isFetching: isUpdating, execute: update } = useFetch(updateUrl, { immediate: false }).put(problem);

const depots = computed(() =>
  uniqBy(
    (problem.value?.vehicles || []).map((v) => v.depot),
    "id"
  )
);

const totalCapacity = computed(() =>
  (problem.value?.vehicles || []).map((it) => it.capacity).reduce((a, b) => a + b, 0)
);
const totalDemand = computed(() => (problem.value?.customers || []).map((it) => it.demand).reduce((a, b) => a + b, 0));
const isValidCapDem = computed(() => totalCapacity.value >= totalDemand.value);

function handleSelectDepot(vehicles: Vehicle[]) {
  if (problem.value) problem.value = { ...problem.value, ...{ vehicles } };
}

function removeVehicle(vehicle: Vehicle) {
  if (problem.value) {
    const filteredVehicles = problem.value?.vehicles?.filter(({ id }) => id !== vehicle.id) || [];
    problem.value = { ...problem.value, ...{ vehicles: filteredVehicles } };
  }
}

function addCustomer(customer: Customer) {
  if (problem.value) {
    const newCustomers = uniqBy((problem.value?.customers || []).concat(customer), "id");
    problem.value = { ...problem.value, ...{ customers: sortBy(newCustomers, (c) => c.name) } };
  }
}

function removeCustomer(customer: Customer) {
  if (problem.value) {
    const filteredCustomers = problem.value?.customers?.filter(({ id }) => id !== customer.id) || [];
    problem.value = { ...problem.value, ...{ customers: filteredCustomers } };
  }
}
</script>

<template>
  <vrp-page-layout v-slot="{ mapFooterHeight }" :is-fetching="isFetching" :error="error">
    <main class="flex my-2 mx-2 space-x-2 h-full" :style="`height: calc(100vh - ${mapFooterHeight})`">
      <div class="flex-initial flex-col w-7/12 space-y-2">
        <div>
          <table class="table table-sm table-zebra">
            <thead>
              <th>Name</th>
              <th>Total Capacity</th>
              <th>Total Demand</th>
            </thead>
            <tbody>
              <td>
                <input v-if="problem" v-model="problem.name" name="name" class="input input-bordered w-full input-xs" />
              </td>
              <td :class="isValidCapDem ? '' : 'text-error'">{{ totalCapacity }}</td>
              <td :class="isValidCapDem ? '' : 'text-error'">{{ totalDemand }}</td>
            </tbody>
          </table>
        </div>

        <div role="tablist" class="tabs tabs-bordered">
          <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Depot" checked />
          <div
            role="tabpanel"
            class="tab-content pt-2 overflow-y-auto overflow-x-hidden"
            :style="`height: calc(100vh - 330px)`"
          >
            <vrp-depot-tab
              v-if="problem"
              :vehicles="problem.vehicles"
              @select-value="handleSelectDepot"
              @remove-vehicle="removeVehicle"
            />
          </div>

          <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Customers" />
          <div
            role="tabpanel"
            class="tab-content pt-2 overflow-y-auto overflow-x-hidden"
            :style="`height: calc(100vh - 330px)`"
          >
            <vrp-customers-tab
              v-if="problem"
              :customers="problem?.customers"
              @remove-customer="removeCustomer"
              @add-customer="addCustomer"
            />
          </div>
        </div>

        <div class="flex flex-row-reverse">
          <form class="space-x-2">
            <router-link to="/" class="btn">Cancel</router-link>
            <button class="btn btn-success" :disabled="isUpdating || !isValidCapDem" @click="() => update()">
              Save<span v-if="isUpdating" class="loading loading-bars loading-xs"></span>
            </button>
          </form>
        </div>
      </div>
      <div class="flex-auto">
        <location-map :locations="depots.concat(problem?.customers || [])" />
      </div>
    </main>
  </vrp-page-layout>
</template>
