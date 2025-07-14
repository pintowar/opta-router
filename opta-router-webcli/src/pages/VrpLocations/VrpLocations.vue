<script lang="ts" setup>
import { computed } from "vue";
import { useCrud } from "../../composables/useCrud";

import type { Customer, Depot } from "../../api";
import { isDepot } from "../../api";

import { CrudActionButtons, LocationMap, PaginatedTable } from "../../components";
import { VrpCrudPageLayout } from "../../layout";
import VrpLocationForm from "./VrpLocationForm.vue";

const baseRestUrl = "/api/vrp-locations";

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
  <vrp-crud-page-layout
    :is-fetching="isFetching"
    :error="error"
    :remove-error="removeError"
    :update-error="updateError"
    :insert-error="insertError"
    :success-update="successUpdate"
    :success-insert="successInsert"
    :remove-url="removeUrl"
    :open-remove="openRemove"
    :selected="selected"
    :open-insert="openInsert"
    title="Locations"
    @close-error="errorClose"
    @close-success="successClose"
    @fetch="fetch"
    @fail-remove="removeError = true"
    @toogle-insert="toogleInsert"
    @update:open-remove="openRemove = $event"
  >
    <div class="flex w-full grow space-x-2 overflow-y-hidden">
      <div class="flex flex-col w-7/12">
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
            <crud-action-buttons :is-editing="false" @edit="editItem(row)" @delete="showDeleteModal(row)" />
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
            <crud-action-buttons
              :is-editing="true"
              :is-updating="isUpdating"
              @update="() => updateItem(selected)"
              @cancel="() => editItem(null)"
            />
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
  </vrp-crud-page-layout>
</template>
