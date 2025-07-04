<script lang="ts" setup>
import { useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";
import { useCrud } from "../../composables/useCrud";

import type { Depot, Vehicle } from "../../api";

import { AlertMessage, DeleteDialog, InputSearch, PaginatedTable } from "../../components";
import { VrpPageLayout } from "../../layout";
import VrpVehicleForm from "./VrpVehicleForm.vue";

const baseRestUrl = "/api/vrp-vehicles";
const route = useRoute();

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
        :message="`Are you sure you want to delete ${selected?.name} (id: ${selected?.id})?`"
        @success-remove="fetch"
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
  </vrp-page-layout>
</template>
