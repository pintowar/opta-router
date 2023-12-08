<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Vehicle, Page, Depot } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import AlertMessage from "../components/AlertMessage.vue";
import PaginatedTable from "../components/PaginatedTable.vue";

const route = useRoute();

const url = computed(() => `/api/vrp-vehicles?page=${route.query.page || 0}&size=${route.query.size || 10}`);
const {
  isFetching,
  data: page,
  error,
  execute: fetchVehicles,
} = useFetch(url, { refetch: true, afterFetch: afterVehiclesFetch }).get().json<Page<Vehicle>>();

const selectedVehicle = ref<Vehicle | null>(null);
const hoveredLine = ref<number | null>(null);
const removeUrl = computed(() => `/api/vrp-vehicles/${selectedVehicle.value?.id}/remove`);
const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

const updateUrl = computed(() => `/api/vrp-vehicles/${selectedVehicle.value?.id}/update`);
const {
  isFetching: isUpdating,
  error: updateError,
  execute: update,
} = useFetch(updateUrl, { immediate: false }).put(selectedVehicle);

const depotsUrl = "/api/vrp-locations/depot";
const { data: depots } = useFetch(depotsUrl, { initialData: [] }).get().json<Depot[]>();

const deleteModal = ref<HTMLDialogElement | null>(null);

function showDeleteModal(vehicle: Vehicle) {
  selectedVehicle.value = vehicle;
  deleteModal?.value?.showModal();
}

async function updateVehicle(vehicle: Vehicle | null) {
  if (vehicle) {
    await update();
    await fetchVehicles();
  }
}

function editVehicle(vehicle: Vehicle | null) {
  selectedVehicle.value = vehicle;
  if (vehicle === null) {
    fetchVehicles();
  }
}

function afterVehiclesFetch(ctx: AfterFetchContext) {
  selectedVehicle.value = null;
  return ctx;
}

async function removeVehicle() {
  await remove();
  selectedVehicle.value = null;
  deleteModal?.value?.close();
  await fetchVehicles();
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main>
      <div class="mx-2 my-2 space-x-2">
        <alert-message
          v-if="removeError || updateError"
          :message="`${removeError ? 'Could not remove Location' : 'Could not update Location'}`"
          variant="error"
        />
        <h1 class="text-2xl">Vehicles</h1>
        <div class="grid justify-items-end my-2 mx-2" data-tip="Create">
          <router-link to="/vehicle/new" class="btn btn-circle">
            <v-icon name="md-add" />
          </router-link>
        </div>

        <paginated-table :page="page" style="height: calc(100vh - 320px)">
          <template #head>
            <tr>
              <th>Id</th>
              <th>Name</th>
              <th>Capacity</th>
              <th>Depot</th>
              <th class="w-24"></th>
            </tr>
          </template>
          <template #body="{ idx, row }">
            <tr
              v-if="row.id !== selectedVehicle?.id"
              :class="`${idx === hoveredLine ? 'hover' : ''}`"
              @mouseenter="() => (hoveredLine = idx)"
              @mouseleave="() => (hoveredLine = null)"
            >
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
                    <v-icon name="la-trash-solid" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-else class="bg-primary-content">
              <td>{{ selectedVehicle.id }}</td>
              <td>
                <input
                  v-model="selectedVehicle.name"
                  :disabled="isUpdating"
                  name="name"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-model="selectedVehicle.capacity"
                  :disabled="isUpdating"
                  name="capacity"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <select v-model="selectedVehicle.depot" class="select select-bordered select-xs">
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
                    <v-icon v-if="!isUpdating" name="bi-check-lg" />
                    <span v-else class="loading loading-bars loading-xs"></span>
                  </button>
                </div>
                <div class="tooltip" data-tip="Cancel">
                  <button class="btn btn-sm btn-circle" @click="() => editVehicle(null)">
                    <v-icon name="bi-x" />
                  </button>
                </div>
              </td>
            </tr>
          </template>
        </paginated-table>

        <dialog id="delete_modal" ref="deleteModal" class="modal">
          <div class="modal-box">
            <form method="dialog">
              <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
            </form>
            <h3 class="font-bold text-lg text-warning">Warning!</h3>
            <p class="py-4">
              Are you sure you want to delete {{ selectedVehicle?.name }} (id: {{ selectedVehicle?.id }})?
            </p>
            <div class="modal-action space-x-2">
              <form method="dialog">
                <button class="btn">Close</button>
              </form>
              <button :disabled="isRemoving" class="btn btn-error" @click="removeVehicle">
                Delete<span v-if="isRemoving" class="loading loading-bars loading-xs"></span>
              </button>
            </div>
          </div>
        </dialog>
      </div>
    </main>
  </vrp-page-layout>
</template>
