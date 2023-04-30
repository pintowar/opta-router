<script lang="ts" setup>
import { ref, onBeforeMount } from "vue";

import { Instance, getInstances } from "../api";

const instances = ref<Instance[]>([]);

onBeforeMount(async () => {
  instances.value = await getInstances();
});
</script>

<template>
  <main class="flex flex-row space-x-4 px-4 py-4">
    <table class="table table-zebra w-full">
      <thead>
        <th>Id</th>
        <th>Name</th>
        <th>Capacity</th>
        <th>Num Locations</th>
        <th>Num Vehicles</th>
        <th></th>
      </thead>
      <tbody v-for="instance in instances" :key="instance.id">
        <td>{{ instance.id }}</td>
        <td>{{ instance.name }}</td>
        <td>{{ instance.capacity }}</td>
        <td>{{ instance.nlocations }}</td>
        <td>{{ instance.nvehicles }}</td>
        <td>
          <router-link :to="'/solve/' + instance.id" class="btn btn-circle">S</router-link>
        </td>
      </tbody>
    </table>
  </main>
</template>
