<script lang="ts" setup>
import { computed, ref } from "vue";
import { useFetch } from "@vueuse/core";

import { Page, VrpProblem } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import AlertMessage from "../components/AlertMessage.vue";
import PaginatedTable from "../components/PaginatedTable.vue";
import { useRoute } from "vue-router";

const route = useRoute();

const url = computed(() => `/api/vrp-problems?page=${route.query.page || 0}&size=${route.query.size || 10}`);
const {
  isFetching,
  error,
  data: page,
  execute: fetchProblems,
} = useFetch(url, { refetch: true }).get().json<Page<VrpProblem>>();

const selectedProblem = ref<VrpProblem | null>(null);
const removeUrl = computed(() => `/api/vrp-problems/${selectedProblem.value?.id}/remove`);
const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

const deleteModal = ref<HTMLDialogElement | null>(null);

const showDeleteModal = (instance: VrpProblem) => {
  selectedProblem.value = instance;
  deleteModal?.value?.showModal();
};

const removeProblem = async () => {
  await remove();
  deleteModal?.value?.close();
  await fetchProblems();
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

        <dialog id="delete_modal" ref="deleteModal" class="modal">
          <div class="modal-box">
            <form method="dialog">
              <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
            </form>
            <h3 class="font-bold text-lg text-warning">Warning!</h3>
            <p class="py-4">
              Are you sure you want to delete {{ selectedProblem?.name }} (id: {{ selectedProblem?.id }})?
            </p>
            <div class="modal-action space-x-2">
              <form method="dialog">
                <button class="btn">Close</button>
              </form>
              <button :disabled="isRemoving" class="btn btn-error" @click="removeProblem">
                Delete<span v-if="isRemoving" class="loading loading-bars loading-xs"></span>
              </button>
            </div>
          </div>
        </dialog>
      </div>
    </main>
  </vrp-page-layout>
</template>
