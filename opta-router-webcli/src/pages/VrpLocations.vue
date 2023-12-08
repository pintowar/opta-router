<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Customer, Depot, Page } from "../api";

import { VrpPageLayout } from "../layout";
import LocationMap from "../components/LocationMap.vue";
import AlertMessage from "../components/AlertMessage.vue";
import PaginatedTable from "../components/PaginatedTable.vue";
import DeleteDialog from "../components/DeleteDialog.vue";

const route = useRoute();

const url = computed(() => `/api/vrp-locations?page=${route.query.page || 0}&size=${route.query.size || 10}`);
const {
  isFetching,
  data: page,
  error,
  execute: fetchLocations,
} = useFetch(url, { refetch: true, afterFetch: afterLocationsFetch }).get().json<Page<Customer | Depot>>();
const locations = computed(() => page.value?.content || []);

const selectedLocation = ref<Customer | Depot | null>(null);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `/api/vrp-locations/${selectedLocation.value?.id}/remove`);
const removeError = ref(false);

const updateUrl = computed(() => `/api/vrp-locations/${selectedLocation.value?.id}/update`);
const {
  isFetching: isUpdating,
  error: updateError,
  execute: update,
} = useFetch(updateUrl, { immediate: false }).put(selectedLocation);

function showDeleteModal(location: Customer | Depot) {
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
  if (location === null) {
    fetchLocations();
  }
}

function afterLocationsFetch(ctx: AfterFetchContext) {
  selectedLocation.value = null;
  return ctx;
}

function isDepot(obj: unknown): obj is Depot {
  return Boolean(obj && typeof obj === "object" && !("demand" in obj));
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main class="flex my-2 mx-2 space-x-2 h-full" style="height: calc(100vh - 155px)">
      <div class="flex-initial flex-col w-7/12 space-y-2">
        <alert-message
          v-if="removeError || updateError"
          :message="`${removeError ? 'Could not remove Location' : 'Could not update Location'}`"
          variant="error"
        />

        <delete-dialog
          v-model:url="removeUrl"
          v-model:open="openRemove"
          :message="`Are you sure you want to delete ${selectedLocation?.name} (id: ${selectedLocation?.id})?`"
          @success-remove="fetchLocations"
          @fail-remove="removeError = true"
        />

        <h1 class="text-2xl">Locations</h1>
        <div class="grid justify-items-end my-2 mx-2" data-tip="Create">
          <router-link to="/location/new" class="btn btn-circle">
            <v-icon name="md-add" />
          </router-link>
        </div>

        <paginated-table :page="page" :selected="selectedLocation" style="height: calc(100vh - 320px)">
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
                  <v-icon name="la-trash-solid" />
                </button>
              </div>
            </td>
          </template>
          <template #edit="{ item }">
            <td>{{ item.id }}</td>
            <td>
              <input
                v-model="item.name"
                :disabled="isUpdating"
                name="name"
                class="input input-bordered w-full input-xs"
              />
            </td>
            <td>
              <input
                v-model.number="item.lat"
                :disabled="isUpdating"
                name="lat"
                class="input input-bordered w-full input-xs"
              />
            </td>
            <td>
              <input
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
                  <v-icon v-if="!isUpdating" name="bi-check-lg" />
                  <span v-else class="loading loading-bars loading-xs"></span>
                </button>
              </div>
              <div class="tooltip" data-tip="Cancel">
                <button class="btn btn-sm btn-circle" @click="() => editLocation(null)">
                  <v-icon name="bi-x" />
                </button>
              </div>
            </td>
          </template>
        </paginated-table>
      </div>
      <div class="flex-auto">
        <location-map v-model:selected-location="selectedLocation" :locations="locations || []" />
      </div>
    </main>
  </vrp-page-layout>
</template>
