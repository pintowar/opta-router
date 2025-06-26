<script lang="ts" setup>
import type { AfterFetchContext } from "@vueuse/core";
import { useFetch } from "@vueuse/core";
import { computed, ref } from "vue";
import { useRoute } from "vue-router";

import type { Depot, Page, Vehicle } from "../../api";

import { AlertMessage, DeleteDialog, InputSearch, PaginatedTable } from "../../components";
import { VrpPageLayout } from "../../layout";
import VrpVehicleForm from "./VrpVehicleForm.vue";

const baseRestUrl = "/api/vrp-vehicles";
const route = useRoute();

const url = computed(
  () => `${baseRestUrl}?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ""}`
);
const {
  isFetching,
  data: page,
  error,
  execute: fetchVehicles,
} = useFetch(url, { refetch: true, afterFetch: afterVehiclesFetch }).get().json<Page<Vehicle>>();

const selectedVehicle = ref<Vehicle | null>(null);

const openInsert = ref<boolean>(false);
const baseIdRestUrl = computed(() => `${baseRestUrl}/${selectedVehicle.value?.id}`);
const insertUrl = `${baseRestUrl}/insert`;
const {
  isFetching: isInserting,
  error: insertError,
  execute: insert,
  statusCode: insertCode,
} = useFetch(insertUrl, { immediate: false }).post(selectedVehicle);
const successInsert = computed(() => (insertCode.value || 0) >= 200 && (insertCode.value || 0) < 300);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `${baseIdRestUrl.value}/remove`);
const removeError = ref(false);

const isEditing = ref(false);
const updateUrl = computed(() => `${baseIdRestUrl.value}/update`);
const {
  isFetching: isUpdating,
  error: updateError,
  execute: update,
  statusCode: updateCode,
} = useFetch(updateUrl, { immediate: false }).put(selectedVehicle);
const successUpdate = computed(() => (updateCode.value || 0) >= 200 && (updateCode.value || 0) < 300);

const depotsUrl = "/api/vrp-locations/depot";
const { data: depots } = useFetch(depotsUrl, { initialData: [] }).get().json<Depot[]>();

function toogleInsert() {
  if (!openInsert.value) {
    const depot = (depots.value && depots.value[0]) || { id: -1, name: "", lat: 0, lng: 0 };
    const newVehicle = { id: -1, name: "", capacity: 0, depot };
    selectedVehicle.value = newVehicle;
  } else {
    selectedVehicle.value = null;
  }
  openInsert.value = !openInsert.value;
}

async function insertVehicle(vehicle: Vehicle | null) {
  if (vehicle) {
    await insert();
    toogleInsert();
    await fetchVehicles();
  }
}

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

function errorClose() {
  removeError.value = false;
  insertError.value = null;
  updateError.value = null;
}

function successClose() {
  updateCode.value = null;
  insertCode.value = null;
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <div class="w-full flex flex-col mx-2 my-2 space-x-2">
      <alert-message
        v-if="removeError || updateError || insertError"
        :message="`${removeError ? 'Could not remove Vehicle' : 'Could not save/update Vehicle'}`"
        variant="error"
        @close="errorClose"
      />

      <alert-message
        v-if="successUpdate || successInsert"
        :message="`${successUpdate ? 'Succcessfully update Vehicle' : 'Succcessfully save Vehicle'}`"
        variant="success"
        @close="successClose"
      />

      <delete-dialog
        v-model:url="removeUrl"
        v-model:open="openRemove"
        :message="`Are you sure you want to delete ${selectedVehicle?.name} (id: ${selectedVehicle?.id})?`"
        @success-remove="fetchVehicles"
        @fail-remove="removeError = true"
      />

      <h1 class="text-2xl">Vehicles</h1>
      <div class="flex justify-between">
        <input-search v-if="!openInsert" :query="`${route.query.q || ''}`" />
        <div v-else></div>
        <button class="btn btn-circle" @click="toogleInsert">
          <v-icon name="md-add" />
        </button>
      </div>

      <div v-if="openInsert">
        <vrp-vehicle-form
          v-if="selectedVehicle"
          v-model:vehicle="selectedVehicle"
          :depots="depots || []"
          :is-loading="isInserting"
          @execute="() => insertVehicle(selectedVehicle)"
          @close="toogleInsert"
        />
      </div>

      <paginated-table v-else :page="page" :selected="selectedVehicle" :is-editing="isEditing">
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
  </vrp-page-layout>
</template>
