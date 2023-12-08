<script lang="ts" setup>
import { computed, ref } from "vue";
import { useFetch } from "@vueuse/core";
import { useRoute } from "vue-router";

import { Page, VrpProblem } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import AlertMessage from "../components/AlertMessage.vue";
import PaginatedTable from "../components/PaginatedTable.vue";
import DeleteDialog from "../components/DeleteDialog.vue";

const route = useRoute();

const url = computed(() => `/api/vrp-problems?page=${route.query.page || 0}&size=${route.query.size || 10}`);
const {
  isFetching,
  error,
  data: page,
  execute: fetchProblems,
} = useFetch(url, { refetch: true }).get().json<Page<VrpProblem>>();

const selectedProblem = ref<VrpProblem | null>(null);

const openRemove = ref<boolean>(false);
const removeUrl = computed(() => `/api/vrp-problems/${selectedProblem.value?.id}/remove`);
const removeError = ref(false);

const showDeleteModal = (instance: VrpProblem) => {
  selectedProblem.value = instance;
  openRemove.value = true;
};
</script>

<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <main>
      <div class="mx-2 my-2 space-x-2">
        <alert-message
          v-if="removeError"
          :message="`Could not remove VrpProblem: ${selectedProblem?.name}`"
          variant="error"
        />

        <delete-dialog
          v-model:url="removeUrl"
          v-model:open="openRemove"
          :message="`Are you sure you want to delete ${selectedProblem?.name} (id: ${selectedProblem?.id})?`"
          @success-remove="fetchProblems"
          @fail-remove="removeError = true"
        />

        <h1 class="text-2xl">Routes</h1>
        <div class="grid justify-items-end my-2 mx-2" data-tip="Create">
          <router-link to="/problem/new" class="btn btn-circle">
            <v-icon name="md-add" />
          </router-link>
        </div>

        <paginated-table :page="page" style="height: calc(100vh - 320px)">
          <template #head>
            <tr>
              <th>Id</th>
              <th>Name</th>
              <th>Num Locations</th>
              <th>Num Vehicles</th>
              <th>Actions</th>
            </tr>
          </template>
          <template #body="{ row }">
            <tr>
              <td>{{ row.id }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.nlocations }}</td>
              <td>{{ row.nvehicles }}</td>
              <td class="space-x-2">
                <div class="tooltip" data-tip="Solve it">
                  <router-link :to="`/solve/${row.id}`" class="btn btn-sm btn-circle">
                    <v-icon name="oi-gear" />
                  </router-link>
                </div>
                <div class="tooltip" data-tip="Edit">
                  <router-link :to="`/problem/${row.id}/edit`" class="btn btn-sm btn-circle">
                    <v-icon name="md-edit-twotone" />
                  </router-link>
                </div>
                <div class="tooltip" data-tip="Delete">
                  <button class="btn btn-sm btn-circle" @click="showDeleteModal(row)">
                    <v-icon name="la-trash-solid" />
                  </button>
                </div>
              </td>
            </tr>
          </template>
        </paginated-table>
      </div>
    </main>
  </vrp-page-layout>
</template>
