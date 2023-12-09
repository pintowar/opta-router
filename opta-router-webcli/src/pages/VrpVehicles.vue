<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Vehicle, Page, Depot } from "../api";

import { VrpPageLayout } from "../layout";
import { AlertMessage, DeleteDialog, InputSearch, PaginatedTable } from "../components";

const route = useRoute();

const url = computed(
  () => `/api/vrp-vehicles?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ""}`
);
const {
  isFetching,
  data: page,
  error,
  execute: fetchVehicles,
} = useFetch(url, { refetch: true, afterFetch: afterVehiclesFetch }).get().json<Page<Vehicle>>();

const selectedVehicle = ref<Vehicle | null>(null);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `/api/vrp-vehicles/${selectedVehicle.value?.id}/remove`);
const removeError = ref(false);

const isEditing = ref(false);
const updateUrl = computed(() => `/api/vrp-vehicles/${selectedVehicle.value?.id}/update`);
const {
  isFetching: isUpdating,
  error: updateError,
  execute: update,
} = useFetch(updateUrl, { immediate: false }).put(selectedVehicle);

const depotsUrl = "/api/vrp-locations/depot";
const { data: depots } = useFetch(depotsUrl, { initialData: [] }).get().json<Depot[]>();

function showDeleteModal(vehicle: Vehicle) {
  isEditing.value = false;
  selectedVehicle.value = vehicle;
  openRemove.value = true;
}

async function updateVehicle(vehicle: Vehicle | null) {
  if (vehicle) {
    await update();
    await fetchVehicles();
  }
}

function editVehicle(vehicle: Vehicle | null) {
  selectedVehicle.value = vehicle;
  isEditing.value = vehicle !== null;
  if (vehicle === null) {
    fetchVehicles();
  }
}

function afterVehiclesFetch(ctx: AfterFetchContext) {
  selectedVehicle.value = null;
  return ctx;
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main>
      <div class="mx-2 my-2 space-x-2">
        <alert-message
          v-if="removeError || updateError"
          :message="`${removeError ? 'Could not remove Vehicle' : 'Could not update Vehicle'}`"
          variant="error"
        />

        <delete-dialog
          v-model:url="removeUrl"
          v-model:open="openRemove"
          :message="`Are you sure you want to delete ${selectedVehicle?.name} (id: ${selectedVehicle?.id})?`"
          @success-remove="fetchVehicles"
          @fail-remove="removeError = true"
        />

        <h1 class="text-2xl">Vehicles</h1>
        <div class="flex w-full justify-between">
          <input-search :query="`${route.query.q || ''}`" />
          <router-link to="/vehicle/new" class="btn btn-circle">
            <v-icon name="md-add" />
          </router-link>
        </div>

        <paginated-table
          :page="page"
          :selected="selectedVehicle"
          :is-editing="isEditing"
          style="height: calc(100vh - 320px)"
        >
          <template #head>
            <th>Id</th>
            <th>Name</th>
            <th>Capacity</th>
            <th>Depot</th>
            <th class="w-24"></th>
          </template>
          <template #show="{ row }">
            <td>{{ row.id }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.capacity }}</td>
            <td>{{ row.depot.name }}</td>
            <td class="space-x-2">
              <div class="tooltip" data-tip="Edit">
                <button class="btn btn-sm btn-circle" @click="editVehicle(row)">
                  <v-icon name="md-edit-twotone" />
                </button>
              </div>
              <div class="tooltip" data-tip="Delete">
                <button class="btn btn-sm btn-circle" @click="showDeleteModal(row)">
                  <v-icon name="md-deleteoutline" />
                </button>
              </div>
            </td>
          </template>
          <template #edit="{ item }">
            <td>{{ item?.id }}</td>
            <td>
              <input
                v-if="item"
                v-model="item.name"
                :disabled="isUpdating"
                name="name"
                class="input input-bordered w-full input-xs"
              />
            </td>
            <td>
              <input
                v-if="item"
                v-model="item.capacity"
                :disabled="isUpdating"
                name="capacity"
                class="input input-bordered w-full input-xs"
              />
            </td>
            <td>
              <select v-if="item" v-model="item.depot" class="select select-bordered select-xs">
                <option v-for="depot in depots" :key="depot.id" :value="depot">
                  {{ depot.name }}
                </option>
              </select>
            </td>
            <td class="space-x-2">
              <div class="tooltip" data-tip="Update">
                <button
                  :disabled="isUpdating"
                  class="btn btn-sm btn-circle"
                  @click="() => updateVehicle(selectedVehicle)"
                >
                  <v-icon v-if="!isUpdating" name="md-check" />
                  <span v-else class="loading loading-bars loading-xs"></span>
                </button>
              </div>
              <div class="tooltip" data-tip="Cancel">
                <button class="btn btn-sm btn-circle" @click="() => editVehicle(null)">
                  <v-icon name="md-close" />
                </button>
              </div>
            </td>
          </template>
        </paginated-table>
      </div>
    </main>
  </vrp-page-layout>
</template>
