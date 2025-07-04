<script lang="ts" setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useCrud } from "../../composables/useCrud";

import type { Customer, Depot } from "../../api";
import { isDepot } from "../../api";

import { AlertMessage, DeleteDialog, InputSearch, LocationMap, PaginatedTable } from "../../components";
import { VrpPageLayout } from "../../layout";
import VrpLocationForm from "./VrpLocationForm.vue";

const baseRestUrl = "/api/vrp-locations";
const route = useRoute();

const {
  isFetching,
  page,
  error,
  fetch,
  selected,
  openInsert,
  isInserting,
  insertError,
  successInsert,
  openRemove,
  removeUrl,
  removeError,
  isEditing,
  isUpdating,
  updateError,
  successUpdate,
  showDeleteModal,
  updateItem,
  editItem,
  errorClose,
  successClose,
  toogleInsert,
  insertItem,
} = useCrud<Customer | Depot>(baseRestUrl, {
  id: -1,
  name: "",
  lat: 0,
  lng: 0,
  demand: 0,
});

const locations = computed(() => page.value?.content || []);

const allLocations = computed(() => {
  if (selected.value) {
    const found = locations.value.find(({ id }) => id === selected.value?.id);
    return found ? locations.value : locations.value.concat([selected.value]);
  } else {
    return locations.value;
  }
});
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <div class="w-full flex my-2 mx-2 space-x-2">
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
          :message="`Are you sure you want to delete ${selected?.name} (id: ${selected?.id})?`"
          @success-remove="fetch"
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

        <div v-if="openInsert">
          <vrp-location-form
            v-if="selected"
            v-model:location="selected"
            :is-loading="isInserting"
            @execute="() => insertItem(selected)"
            @close="toogleInsert"
          />
        </div>

        <paginated-table v-else :page="page" :selected="selected" :is-editing="isEditing">
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
                <button class="btn btn-sm btn-circle" @click="editItem(row)">
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
                v-if="!isDepot(selected)"
                v-model.number="(item as Customer).demand"
                :disabled="isUpdating"
                name="demand"
                class="input input-bordered w-full input-xs"
              />
            </td>
            <td>{{ isDepot(selected) ? "Depot" : "Customer" }}</td>
            <td class="space-x-2">
              <div class="tooltip" data-tip="Update">
                <button
                  :disabled="isUpdating"
                  class="btn btn-sm btn-circle"
                  @click="() => updateItem(selected)"
                >
                  <v-icon v-if="!isUpdating" name="md-check" />
                  <span v-else class="loading loading-bars loading-xs"></span>
                </button>
              </div>
              <div class="tooltip" data-tip="Cancel">
                <button class="btn btn-sm btn-circle" @click="() => editItem(null)">
                  <v-icon name="md-close" />
                </button>
              </div>
            </td>
          </template>
        </paginated-table>
      </div>
      <div class="flex-auto flex-shrink-0">
        <location-map
          v-if="!openInsert"
          v-model:selected-location="selected"
          :edit-mode="isEditing"
          :locations="locations || []"
          @marker-click="isEditing = true"
        />
        <location-map v-else v-model:selected-location="selected" :edit-mode="true" :locations="allLocations" />
      </div>
    </div>
  </vrp-page-layout>
</template>
