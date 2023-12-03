<script setup lang="ts">
import VrpPageLayout from "../layout/VrpPageLayout.vue";
import { computed } from "vue";
import { Customer, Depot, Vehicle, VrpProblem, VrpSolution } from "../api";
import { useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";
// import SolverMap from "../components/SolverMap.vue";
import LocationMap from "../components/LocationMap.vue";

const route = useRoute();

const problemUrl = computed(() => `/api/vrp-problems/${route.params.id}`);
const updateUrl = computed(() => `${problemUrl.value}/update`);
const { isFetching, error, data: problem } = useFetch(problemUrl).get().json<VrpProblem>();
const { isFetching: isUpdating, execute: update } = useFetch(updateUrl).put(problem);
const solution = computed<VrpSolution | null>(() =>
  problem.value
    ? {
        problem: problem.value,
        routes: [],
        empty: true,
        totalDistance: 0,
        feasible: false,
        totalTime: 0,
      }
    : null
);
const customers = computed<Customer[]>(() => problem?.value?.customers || []);
const depots = computed<Depot[]>(() => problem?.value?.depots || []);
const vehicles = computed<Vehicle[]>(() => problem?.value?.vehicles || []);

const totalCapacity = computed(() => vehicles.value.map((it) => it.capacity).reduce((a, b) => a + b, 0));
const totalDemand = computed(() => customers.value.map((it) => it.demand).reduce((a, b) => a + b, 0));
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main class="flex my-2 mx-2 space-x-2 h-full" style="height: calc(100vh - 140px)">
      <div class="flex-initial flex-col w-7/12 space-y-2">
        <div>
          <table class="table table-sm table-zebra">
            <thead>
              <th>Name</th>
              <th>Total Capacity</th>
              <th>Total Demand</th>
            </thead>
            <tbody>
              <td>{{ problem?.name }}</td>
              <td>{{ totalCapacity }}</td>
              <td>{{ totalDemand }}</td>
            </tbody>
          </table>
        </div>

        <div role="tablist" class="tabs tabs-bordered">
          <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Depot" checked />
          <div role="tabpanel" class="tab-content">
            <div v-for="depot in depots" :key="depot.id">
              <span class="indent-4">{{ depot.name }} ({{ depot.lat }}, {{ depot.lng }})</span>
              <table class="table table-sm table-zebra w-full">
                <thead>
                  <th>Name</th>
                  <th>Capacity</th>
                </thead>
                <tbody v-for="vehicle in vehicles" :key="vehicle.id">
                  <td>{{ vehicle.name }}</td>
                  <td>{{ vehicle.capacity }}</td>
                </tbody>
              </table>
            </div>
          </div>

          <input type="radio" name="my_tabs_2" role="tab" class="tab" aria-label="Customers" />
          <div role="tabpanel" class="tab-content overflow-x-auto" style="height: calc(100vh - 370px)">
            <table class="table table-sm table-zebra w-full">
              <thead>
                <th>Name</th>
                <th>Lat</th>
                <th>Lng</th>
                <th>Demand</th>
              </thead>
              <tbody v-for="customer in customers" :key="customer.id">
                <td>{{ customer.name }}</td>
                <td>{{ customer.lat }}</td>
                <td>{{ customer.lng }}</td>
                <td>{{ customer.demand }}</td>
              </tbody>
            </table>
          </div>
        </div>

        <div class="flex flex-row-reverse">
          <form class="space-x-2">
            <router-link to="/" class="btn">Cancel</router-link>
            <button class="btn btn-success" @click="() => update()">
              Save<span v-if="isUpdating" class="loading loading-bars loading-xs"></span>
            </button>
          </form>
        </div>
      </div>
      <div class="flex-auto">
        <location-map v-if="solution" :locations="depots.concat(customers)" />
      </div>
    </main>
  </vrp-page-layout>
</template>
