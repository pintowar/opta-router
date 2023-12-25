<script setup lang="ts">
import { StyleValue, computed, toRefs } from "vue";
import { useFetch, useVModels } from "@vueuse/core";
import { uniqBy, sortBy } from "lodash";
import { Customer, EditableVrpProblem, Vehicle } from "../../../api";
import { LocationMap } from "../../../components";
import VrpDepotTab from "./VrpDepotTab.vue";
import VrpCustomersTab from "./VrpCustomersTab.vue";

const props = defineProps<{
  persistUrl: string;
  problem: EditableVrpProblem;
  style?: StyleValue;
}>();

const emit = defineEmits<{
  (e: "update:problem", val: EditableVrpProblem): void;
}>();

const { persistUrl } = toRefs(props);

const { problem } = useVModels(props, emit);

const fetcher = useFetch(persistUrl, { immediate: false });
const creationMethod = computed(() => !persistUrl.value.endsWith("update"));
const { isFetching: isUpdating, execute: persist } = creationMethod.value ? fetcher.post(problem) : fetcher.put(problem);

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
    <div class="flex my-2 mx-2 space-x-2 h-full" :style="style">
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
            <button class="btn btn-success" :disabled="isUpdating || !isValidCapDem" @click="() => persist()">
              Save<span v-if="isUpdating" class="loading loading-bars loading-xs"></span>
            </button>
          </form>
        </div>
      </div>
      <div class="flex-auto">
        <location-map :locations="depots.concat(problem?.customers || [])" />
      </div>
    </div>
</template>
