<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Customer, Depot, Page, isDepot } from "../../api";

import { VrpPageLayout } from "../../layout";
import VrpLocationForm from "./VrpLocationForm.vue";
import { AlertMessage, DeleteDialog, InputSearch, LocationMap, PaginatedTable } from "../../components";

const baseRestUrl = "/api/vrp-locations";
const route = useRoute();

const url = computed(
  () => `${baseRestUrl}?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ""}`
);
const {
  isFetching,
  data: page,
  error,
  execute: fetchLocations,
} = useFetch(url, { refetch: true, afterFetch: afterLocationsFetch }).get().json<Page<Customer | Depot>>();
const locations = computed(() => page.value?.content || []);

const selectedLocation = ref<Customer | Depot | null>(null);

const allLocations = computed(() => {
  if (selectedLocation.value) {
    const found = locations.value.find(({ id }) => id === selectedLocation.value?.id);
    return found ? locations.value : locations.value.concat([selectedLocation.value]);
  } else {
    return locations.value;
  }
});

const openInsert = ref<boolean>(false);
const baseIdRestUrl = computed(() => `${baseRestUrl}/${selectedLocation.value?.id}`);
const insertUrl = `${baseRestUrl}/insert`;
const {
  isFetching: isInserting,
  error: insertError,
  execute: insert,
  statusCode: insertCode,
} = useFetch(insertUrl, { immediate: false }).post(selectedLocation);
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
} = useFetch(updateUrl, { immediate: false }).put(selectedLocation);
const successUpdate = computed(() => (updateCode.value || 0) >= 200 && (updateCode.value || 0) < 300);

function toogleInsert() {
  if (!openInsert.value) {
    const accCoord = locations.value.reduce(
      (acc, { lat, lng }) => ({
        lat: acc.lat + lat,
        lng: acc.lng + lng,
      }),
      { lat: 0, lng: 0 }
    );
    const newCoord = {
      lat: Number((accCoord.lat / locations.value.length).toFixed(6)),
      lng: Number((accCoord.lng / locations.value.length).toFixed(6)),
    };
    const newLocation = { id: -1, name: "", demand: 0, ...newCoord };

    selectedLocation.value = newLocation;
  } else {
    selectedLocation.value = null;
  }
  openInsert.value = !openInsert.value;
}

async function insertLocation(location: Customer | Depot | null) {
  if (location) {
    await insert();
    toogleInsert();
    await fetchLocations();
  }
}

function showDeleteModal(location: Customer | Depot) {
  isEditing.value = false;
  selectedLocation.value = location;
  openRemove.value = true;
}

async function updateLocation(location: Customer | Depot | null) {
  if (location) {
    await update();
    await fetchLocations();
  }
}

function editLocation(location: Customer | Depot | null) {
  selectedLocation.value = location;
  isEditing.value = location !== null;
  if (location === null) {
    fetchLocations();
  }
}

function afterLocationsFetch(ctx: AfterFetchContext) {
  selectedLocation.value = null;
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
  <vrp-page-layout v-slot="{ tableFooterHeight }" :is-fetching="isFetching" :error="error">
    <main>
      <div class="flex my-2 mx-2 space-x-2">
        <div class="flex flex-col w-7/12">
          <alert-message
            v-if="removeError || updateError || insertError"
            :message="`${removeError ? 'Could not remove Location' : 'Could not save/update Location'}`"
            variant="error"
            @close="errorClose"
          />

          <alert-message
            v-if="successUpdate || successInsert"
            :message="`${successUpdate ? 'Succcessfully update Location' : 'Succcessfully save Location'}`"
            variant="success"
            @close="successClose"
          />

          <delete-dialog
            v-model:url="removeUrl"
            v-model:open="openRemove"
            :message="`Are you sure you want to delete ${selectedLocation?.name} (id: ${selectedLocation?.id})?`"
            @success-remove="fetchLocations"
            @fail-remove="removeError = true"
          />

          <h1 class="text-2xl">Locations</h1>
          <div class="flex w-full justify-between">
            <input-search v-if="!openInsert" :query="`${route.query.q || ''}`" />
            <div v-else></div>
            <button class="btn btn-circle" @click="toogleInsert">
              <v-icon :name="`${!openInsert ? 'md-add' : 'md-close'}`" />
            </button>
          </div>

          <div v-if="openInsert" :style="`height: calc(100vh - ${tableFooterHeight})`">
            <vrp-location-form
              v-if="selectedLocation"
              v-model:location="selectedLocation"
              :is-loading="isInserting"
              @execute="() => insertLocation(selectedLocation)"
              @close="toogleInsert"
            />
          </div>

          <paginated-table
            v-else
            :page="page"
            :selected="selectedLocation"
            :is-editing="isEditing"
            :style="`height: calc(100vh - ${tableFooterHeight})`"
          >
            <template #head>
              <th>Id</th>
              <th>Name</th>
              <th>Latitude</th>
              <th>Longitude</th>
              <th>Demand</th>
              <th>Type</th>
              <th class="w-24"></th>
            </template>
            <template #show="{ row }">
              <td>{{ row.id }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.lat }}</td>
              <td>{{ row.lng }}</td>
              <td>{{ isDepot(row) ? "" : (row as Customer).demand }}</td>
              <td>{{ isDepot(row) ? "Depot" : "Customer" }}</td>
              <td class="space-x-2">
                <div class="tooltip" data-tip="Edit">
                  <button class="btn btn-sm btn-circle" @click="editLocation(row)">
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
                  v-model.number="item.lat"
                  :disabled="isUpdating"
                  name="lat"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-if="item"
                  v-model.number="item.lng"
                  :disabled="isUpdating"
                  name="lng"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-if="!isDepot(selectedLocation)"
                  v-model.number="(item as Customer).demand"
                  :disabled="isUpdating"
                  name="demand"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>{{ isDepot(selectedLocation) ? "Depot" : "Customer" }}</td>
              <td class="space-x-2">
                <div class="tooltip" data-tip="Update">
                  <button
                    :disabled="isUpdating"
                    class="btn btn-sm btn-circle"
                    @click="() => updateLocation(selectedLocation)"
                  >
                    <v-icon v-if="!isUpdating" name="md-check" />
                    <span v-else class="loading loading-bars loading-xs"></span>
                  </button>
                </div>
                <div class="tooltip" data-tip="Cancel">
                  <button class="btn btn-sm btn-circle" @click="() => editLocation(null)">
                    <v-icon name="md-close" />
                  </button>
                </div>
              </td>
            </template>
          </paginated-table>
        </div>
        <div class="flex-auto">
          <location-map
            v-if="!openInsert"
            v-model:selected-location="selectedLocation"
            :edit-mode="isEditing"
            :locations="locations || []"
            @marker-click="isEditing = true"
          />
          <location-map
            v-else
            v-model:selected-location="selectedLocation"
            :edit-mode="true"
            :locations="allLocations"
          />
        </div>
      </div>
    </main>
  </vrp-page-layout>
</template>
