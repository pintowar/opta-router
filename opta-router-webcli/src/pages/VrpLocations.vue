<script lang="ts" setup>
import { computed, ref } from "vue";
import { AfterFetchContext, useFetch } from "@vueuse/core";
import { useRoute, useRouter } from "vue-router";

import { Customer, Depot, Page } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import LocationMap from "../components/LocationMap.vue";
import AlertMessage from "../components/AlertMessage.vue";

const router = useRouter();
const route = useRoute();

const selectedLocation = ref<Customer | Depot | null>(null);

const url = computed(() => `/api/vrp-locations?page=${route.query.page || 0}&size=${route.query.size || 10}`);
const {
  isFetching,
  data: page,
  error,
  execute: fetchLocations,
} = useFetch(url, { refetch: true, afterFetch: afterLocationsFetch }).get().json<Page<Customer | Depot>>();
const locations = computed(() => page.value?.content || []);

const hoveredLine = ref<number | null>(null);
const removeUrl = computed(() => `/api/vrp-locations/${selectedLocation.value?.id}/remove`);
const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

const deleteModal = ref<HTMLDialogElement | null>(null);

function showDeleteModal(location: Customer | Depot) {
  selectedLocation.value = location;
  deleteModal?.value?.showModal();
}

function editLocation(location: Customer | Depot | null) {
  selectedLocation.value = location;
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

function paginate(next: number) {
  router.push({
    query: {
      ...route.query,
      page: (page.value?.number || 0) + next,
    },
  });
}
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main class="flex my-2 mx-2 space-x-2 h-full" style="height: calc(100vh - 155px)">
      <div class="flex-initial flex-col w-7/12 space-y-2">
        <alert-message
          v-if="removeError"
          :message="`Could not remove Location: ${selectedLocation?.name}`"
          variant="error"
        />
        <h1 class="text-2xl">Locations</h1>

        <table class="table table-sm table-zebra w-full">
          <thead>
            <th>Id</th>
            <th>Name</th>
            <th>Latitude</th>
            <th>Longitude</th>
            <th>Type</th>
            <th class="w-24"></th>
          </thead>
          <tbody v-for="(location, idx) in locations" :key="location.id">
            <tr
              v-if="location.id !== selectedLocation?.id"
              :class="`${idx === hoveredLine ? 'hover' : ''}`"
              @mouseenter="() => (hoveredLine = idx)"
              @mouseleave="() => (hoveredLine = null)"
            >
              <td>{{ location.id }}</td>
              <td>{{ location.name }}</td>
              <td>{{ location.lat }}</td>
              <td>{{ location.lng }}</td>
              <td>{{ isDepot(location) ? "Depot" : "Customer" }}</td>
              <td class="space-x-2">
                <div class="tooltip" data-tip="Edit">
                  <button class="btn btn-sm btn-circle" @click="editLocation(location)">
                    <v-icon name="md-edit-twotone" />
                  </button>
                </div>
                <div class="tooltip" data-tip="Delete">
                  <button class="btn btn-sm btn-circle" @click="showDeleteModal(location)">
                    <v-icon name="la-trash-solid" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-else class="bg-primary-content">
              <td>{{ location.id }}</td>
              <td>
                <input v-model="selectedLocation.name" name="name" class="input input-bordered w-full input-xs" />
              </td>
              <td>
                <input v-model.number="selectedLocation.lat" name="lat" class="input input-bordered w-full input-xs" />
              </td>
              <td>
                <input v-model.number="selectedLocation.lng" name="lng" class="input input-bordered w-full input-xs" />
              </td>
              <td>{{ isDepot(location) ? "Depot" : "Customer" }}</td>
              <td class="space-x-2">
                <div class="tooltip" data-tip="Update">
                  <button class="btn btn-sm btn-circle">
                    <v-icon name="bi-check-lg" />
                  </button>
                </div>
                <div class="tooltip" data-tip="Cancel">
                  <button class="btn btn-sm btn-circle" @click="() => editLocation(null)">
                    <v-icon name="bi-x" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="join justify-center w-full">
          <button :class="`join-item btn ${page?.first ? 'btn-disabled' : ''}`" @click="paginate(-1)">«</button>
          <button class="join-item btn">Page {{ (page?.number || 0) + 1 }}</button>
          <button :class="`join-item btn ${page?.last ? 'btn-disabled' : ''}`" @click="paginate(1)">»</button>
        </div>

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
              <button class="btn btn-error" @click="removeLocation">
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
