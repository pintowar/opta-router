<script lang="ts" setup>
import { ref } from "vue";
import { useFetch } from "@vueuse/core";

import { VrpProblem } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import IconType from "../components/IconType.vue";

const url = ref("/api/vrp-problems");
const { isFetching, error, data: instances } = useFetch(url).get().json<VrpProblem[]>();
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main>
      <div class="mx-2 my-2 space-x-2">
        <h1 class="text-2xl">Routes</h1>
        <div class="grid justify-items-end my-2 mx-2" data-tip="Create">
          <router-link to="/problem/new" class="btn btn-circle"> New </router-link>
        </div>
        <table class="table table-zebra w-full">
          <thead>
            <th>Id</th>
            <th>Name</th>
            <th>Num Locations</th>
            <th>Num Vehicles</th>
            <th>Actions</th>
          </thead>
          <tbody v-for="instance in instances" :key="instance.id">
            <td>{{ instance.id }}</td>
            <td>{{ instance.name }}</td>
            <td>{{ instance.nlocations }}</td>
            <td>{{ instance.nvehicles }}</td>
            <td class="space-x-2">
              <div class="tooltip" data-tip="Solve it">
                <router-link :to="`/solve/${instance.id}`" class="btn btn-circle">
                  <icon-type name="gears" />
                </router-link>
              </div>
              <div class="tooltip" data-tip="Edit">
                <router-link :to="`/problem/${instance.id}/edit`" class="btn btn-circle">
                  <icon-type name="edit" />
                </router-link>
              </div>
              <div class="tooltip" data-tip="Delete">
                <button class="btn btn-circle">
                  <icon-type name="trash" />
                </button>
              </div>
            </td>
          </tbody>
        </table>
      </div>
    </main>
  </vrp-page-layout>
</template>
