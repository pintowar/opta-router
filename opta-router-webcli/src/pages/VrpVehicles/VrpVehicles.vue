<script lang="ts" setup>
import { useFetch } from "@vueuse/core";
import { useCrud } from "../../composables/useCrud";

import type { Depot, Vehicle } from "../../api";

import { CrudActionButtons, CrudPageLayout, PaginatedTable } from "../../components";
import VrpVehicleForm from "./VrpVehicleForm.vue";

const baseRestUrl = "/api/vrp-vehicles";

const depotsUrl = "/api/vrp-locations/depot";
const { data: depots } = useFetch(depotsUrl, { initialData: [] }).get().json<Depot[]>();

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
} = useCrud<Vehicle>(baseRestUrl, {
  id: -1,
  name: "",
  capacity: 0,
  depot: (depots.value && depots.value[0]) || { id: -1, name: "", lat: 0, lng: 0 },
});
</script>

<template>
  <crud-page-layout
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
    title="Vehicles"
    @close-error="errorClose"
    @close-success="successClose"
    @fetch="fetch"
    @fail-remove="removeError = true"
    @toogle-insert="toogleInsert"
    @update:open-remove="openRemove = $event"
  >
    <div v-if="openInsert">
      <vrp-vehicle-form
        v-if="selected"
        v-model:vehicle="selected"
        :depots="depots || []"
        :is-loading="isInserting"
        @execute="() => insertItem(selected)"
        @close="toogleInsert"
      />
    </div>

    <paginated-table v-else :page="page" :selected="selected" :is-editing="isEditing">
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
        <crud-action-buttons
          :is-editing="true"
          :is-updating="isUpdating"
          @update="() => updateItem(selected)"
          @cancel="() => editItem(null)"
        />
      </template>
    </paginated-table>
  </crud-page-layout>
</template>
