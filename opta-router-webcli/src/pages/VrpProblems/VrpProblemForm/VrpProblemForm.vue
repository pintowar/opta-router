<script setup lang="ts">
import { useFetch, useVModels } from "@vueuse/core";
import { sortBy, uniqBy } from "lodash";
import { computed, toRefs } from "vue";
import type { Customer, Vehicle, VrpProblem } from "../../../api";
import { AlertMessage, LocationMap } from "../../../components";
import VrpCustomersTab from "./VrpCustomersTab.vue";
import VrpVehiclesTab from "./VrpVehiclesTab.vue";

const props = defineProps<{
  persistUrl: string;
  problem: VrpProblem;
}>();

const emit = defineEmits<(event: "update:problem", val: VrpProblem) => void>();

const { persistUrl } = toRefs(props);

const { problem } = useVModels(props, emit);

const fetcher = useFetch(persistUrl, { immediate: false });
const creationMethod = computed(() => !persistUrl.value.endsWith("update"));
const {
  isFetching: isUpdating,
  execute: persist,
  error: persistError,
  statusCode: updateCode,
} = creationMethod.value ? fetcher.post(problem) : fetcher.put(problem);
const successPersist = computed(() => (updateCode.value || 0) >= 200 && (updateCode.value || 0) < 300);

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

function changeCapacity(vehicle: Vehicle) {
  if (problem.value) {
    const idx = problem.value.vehicles.findIndex((v) => v.id === vehicle.id);
    const copy = [...problem.value.vehicles];
    copy.splice(idx, 1, vehicle);
    problem.value = { ...problem.value, ...{ vehicles: copy } };
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

function changeDemand(customer: Customer) {
  if (problem.value) {
    const idx = problem.value.customers.findIndex((c) => c.id === customer.id);
    const copy = [...problem.value.customers];
    copy.splice(idx, 1, customer);
    problem.value = { ...problem.value, ...{ customers: copy } };
  }
}

function errorClose() {
  persistError.value = null;
}

function successClose() {
  updateCode.value = null;
}
</script>

<template>
  <div class="w-full flex my-2 mx-2 space-x-2">
    <div class="flex flex-col w-full space-y-2">
      <h1 class="text-2xl">Routes</h1>
      <div class="flex w-full justify-between">
        <div></div>
        <router-link to="/" class="btn btn-circle">
          <v-icon name="md-close" />
        </router-link>
      </div>
      <div class="flex w-full grow space-x-2 overflow-y-hidden">
        <div class="flex flex-col w-7/12">
          <alert-message
            v-if="persistError"
            message="Could not save/update VrpProblem"
            variant="error"
            @close="errorClose"
          />

          <alert-message
            v-if="successPersist"
            message="Succcessfully save/update Location"
            variant="success"
            @close="successClose"
          />

          <div class="pb-2">
            <table class="table table-sm table-zebra">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Total Capacity</th>
                  <th>Total Demand</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>
                    <input
                      v-if="problem"
                      v-model="problem.name"
                      name="name"
                      class="input input-bordered w-full input-xs"
                    />
                  </td>
                  <td :class="isValidCapDem ? '' : 'text-error'">{{ totalCapacity }}</td>
                  <td :class="isValidCapDem ? '' : 'text-error'">{{ totalDemand }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="flex flex-col grow place-content-between overflow-hidden space-y-2">
            <div role="tablist" class="tabs tabs-bordered overflow-hidden">
              <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Vehicles" checked />
              <div role="tabpanel" class="tab-content pt-2 overflow-x-hidden">
                <vrp-vehicles-tab
                  v-if="problem"
                  :vehicles="problem.vehicles"
                  @select-value="handleSelectDepot"
                  @remove-vehicle="removeVehicle"
                  @change-capacity="changeCapacity"
                />
              </div>

              <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Customers" />
              <div role="tabpanel" class="tab-content pt-2 overflow-x-hidden">
                <vrp-customers-tab
                  v-if="problem"
                  :customers="problem?.customers"
                  @remove-customer="removeCustomer"
                  @add-customer="addCustomer"
                  @change-demand="changeDemand"
                />
              </div>
            </div>

            <div class="flex flex-row-reverse pt-2">
              <form class="space-x-2">
                <router-link to="/" class="btn">Cancel</router-link>
                <button
                  type="button"
                  class="btn btn-success"
                  :disabled="isUpdating || !isValidCapDem"
                  @click="() => persist()"
                >
                  Save<span v-if="isUpdating" class="loading loading-bars loading-xs"></span>
                </button>
              </form>
            </div>
          </div>
        </div>
        <div class="flex-auto flex-shrink-0">
          <location-map :locations="depots.concat(problem?.customers || [])" />
        </div>
      </div>
    </div>
  </div>
</template>
