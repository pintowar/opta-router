<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Customer, Depot, Page } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import LocationMap from "../components/LocationMap.vue";
import AlertMessage from "../components/AlertMessage.vue";
import PaginatedTable from "../components/PaginatedTable.vue";

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
const hoveredLine = ref<number | null>(null);
const removeUrl = computed(() => `/api/vrp-locations/${selectedLocation.value?.id}/remove`);
const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

const updateUrl = computed(() => `/api/vrp-locations/${selectedLocation.value?.id}/update`);
const {
  isFetching: isUpdating,
  error: updateError,
  execute: update,
} = useFetch(updateUrl, { immediate: false }).put(selectedLocation);

const deleteModal = ref<HTMLDialogElement | null>(null);

function showDeleteModal(location: Customer | Depot) {
  selectedLocation.value = location;
  deleteModal?.value?.showModal();
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

async function removeLocation() {
  await remove();
  selectedLocation.value = null;
  deleteModal?.value?.close();
  await fetchLocations();
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
        <h1 class="text-2xl">Locations</h1>

        <paginated-table :page="page" style="height: calc(100vh - 260px)">
          <template #head>
            <tr>
              <th>Id</th>
              <th>Name</th>
              <th>Latitude</th>
              <th>Longitude</th>
              <th>Demand</th>
              <th>Type</th>
              <th class="w-24"></th>
            </tr>
          </template>
          <template #body="{ idx, row }">
            <tr
              v-if="row.id !== selectedLocation?.id"
              :class="`${idx === hoveredLine ? 'hover' : ''}`"
              @mouseenter="() => (hoveredLine = idx)"
              @mouseleave="() => (hoveredLine = null)"
            >
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
            </tr>
            <tr v-else class="bg-primary-content">
              <td>{{ selectedLocation.id }}</td>
              <td>
                <input
                  v-model="selectedLocation.name"
                  :disabled="isUpdating"
                  name="name"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-model.number="selectedLocation.lat"
                  :disabled="isUpdating"
                  name="lat"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-model.number="selectedLocation.lng"
                  :disabled="isUpdating"
                  name="lng"
                  class="input input-bordered w-full input-xs"
                />
              </td>
              <td>
                <input
                  v-if="!isDepot(selectedLocation)"
                  v-model.number="(selectedLocation as Customer).demand"
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
              Are you sure you want to delete {{ selectedLocation?.name }} (id: {{ selectedLocation?.id }})?
            </p>
            <div class="modal-action space-x-2">
              <form method="dialog">
                <button class="btn">Close</button>
              </form>
              <button :disabled="isRemoving" class="btn btn-error" @click="removeLocation">
                Delete<span v-if="isRemoving" class="loading loading-bars loading-xs"></span>
              </button>
            </div>
          </div>
        </dialog>
      </div>
      <div class="flex-auto">
        <location-map v-model:selected-location="selectedLocation" :locations="locations || []" />
      </div>
    </main>
  </vrp-page-layout>
</template>
