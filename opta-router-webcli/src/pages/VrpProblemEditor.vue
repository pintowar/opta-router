<script setup lang="ts">
import VrpPageLayout from "../layout/VrpPageLayout.vue";
import { computed } from "vue";
import {Customer, Depot, Vehicle, VrpProblem, VrpSolution} from "../api";
import {useFetch} from "@vueuse/core";
import {useRoute} from "vue-router";
import SolverMap from "../components/SolverMap.vue";

const route = useRoute();

const problemUrl = computed(() => `/api/vrp-problems/${route.params.id}`);
const { isFetching, error, data: problem } = useFetch(problemUrl).get().json<VrpProblem>();
const solution = computed<VrpSolution>(() => ({
    problem: problem.value,
    routes: [],
    empty: true,
    totalDistance: 0,
    feasible: false,
    totalTime: 0,
}));
const customers = computed<Customer>(() => problem?.value?.customers || []);
const depots = computed<Depot>(() => problem?.value?.depots || []);
const vehicles = computed<Vehicle>(() => problem?.value?.vehicles || []);

</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
      <main class="flex my-2 mx-2 space-x-2 h-full" style="height: calc(100vh - 140px)">
          <div class="flex-initial w-7/12">
              <div>
                  <h3 class="text-2xl indent-1">Depot</h3>
                      <div v-for="depot in depots" :key="depot.id">
                        <span class="indent-4">{{depot.name}} ({{depot.lat}}, {{depot.lng}})</span>
                          <table class="table table-sm table-zebra w-full">
                              <thead>
                                  <th>Name</th>
                                  <th>Capacity</th>
                              </thead>
                              <tbody v-for="vehicle in vehicles" :key="vehicle.id">
                                  <td>{{vehicle.name}}</td>
                                  <td>{{vehicle.capacity}}</td>
                              </tbody>
                          </table>
                      </div>

              </div>
              <div class="overflow-x-auto h-96">
                  <h3 class="text-2xl indent-1">Customers</h3>
                  <table class="table table-sm table-zebra w-full">
                      <thead>
                          <th>Name</th>
                          <th>Lat</th>
                          <th>Lng</th>
                          <th>Demand</th>
                      </thead>
                      <tbody v-for="customer in customers" :key="customer.id">
                        <td>{{customer.name}}</td>
                        <td>{{customer.lat}}</td>
                        <td>{{customer.lng}}</td>
                        <td>{{customer.demand}}</td>
                      </tbody>
                  </table>
              </div>
          </div>
          <div class="flex-auto">
              <solver-map :solution="solution" />
          </div>
<!--          <div class="flex my-2 mx-2 space-x-2" style="height: calc(100vh - 140px)">
              <div class="flex-initial w-6/12">
                  <div class="flex h-24">
                      <h1>Customers</h1>
                      <table class="table-sm table-zebra w-full">
                          <thead>
                              <th>Name</th>
                              <th>Lat</th>
                              <th>Lng</th>
                              <th>Demand</th>
                          </thead>
                          <tbody v-for="customer in problem.customers" :key="customer.id">
                              <td>{{customer.name}}</td>
                              <td>{{customer.lat}}</td>
                              <td>{{customer.lng}}</td>
                              <td>{{customer.demand}}</td>
                          </tbody>
                      </table>
                  </div>
                  <div class="flex h-24">
                      <h1>Vehicles</h1>
                      <table class="table-sm table-zebra w-full">
                          <thead>
                              <th>Name</th>
                              <th>Capacity</th>
                          </thead>
                          <tbody v-for="vehicle in problem.vehicles" :key="vehicle.id">
                              <td>{{vehicle.name}}</td>
                              <td>{{vehicle.capacity}}</td>
                          </tbody>
                      </table>
                  </div>
              </div>
              <div class="flex-auto">
                <solver-map :solution="solution" />
              </div>
          </div>-->
      </main>
  </vrp-page-layout>
</template>
