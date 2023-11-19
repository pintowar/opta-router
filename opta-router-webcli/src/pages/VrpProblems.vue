<script lang="ts" setup>
import { computed, ref } from "vue";
import { useFetch } from "@vueuse/core";

import { VrpProblem } from "../api";

import VrpPageLayout from "../layout/VrpPageLayout.vue";
import IconType from "../components/IconType.vue";

const url = ref("/api/vrp-problems");
const { isFetching, error, data: instances, execute: fetchProblems } = useFetch(url).get().json<VrpProblem[]>();

const selectedProblem = ref<VrpProblem | null>(null);
const removeUrl = computed(() => `/api/vrp-problems/${selectedProblem.value?.id}/remove`);
const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

const deleteModal = ref<typeof HTMLDialogElement | null>(null);

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
        <div v-if="removeError" role="alert" class="alert alert-error">
          <icon-type name="error" />
          <span>Could not remove VrpProblem: {{ selectedProblem?.name }}</span>
        </div>
        <h1 class="text-2xl">Routes</h1>
        <div class="grid justify-items-end my-2 mx-2" data-tip="Create">
          <router-link to="/problem/new" class="btn btn-circle"> New </router-link>
        </div>

        <table class="table table-zebra w-full">
          <thead>
            <th>Id</th>
            <th>Name</th>
            <th>Num Locations</th>
            <th>Num Vehicles</th>
            <th>Actions</th>
          </thead>
          <tbody v-for="instance in instances" :key="instance.id">
            <td>{{ instance.id }}</td>
            <td>{{ instance.name }}</td>
            <td>{{ instance.nlocations }}</td>
            <td>{{ instance.nvehicles }}</td>
            <td class="space-x-2">
              <div class="tooltip" data-tip="Solve it">
                <router-link :to="`/solve/${instance.id}`" class="btn btn-circle">
                  <icon-type name="gears" />
                </router-link>
              </div>
              <div class="tooltip" data-tip="Edit">
                <router-link :to="`/problem/${instance.id}/edit`" class="btn btn-circle">
                  <icon-type name="edit" />
                </router-link>
              </div>
              <div class="tooltip" data-tip="Delete">
                <button class="btn btn-circle" @click="showDeleteModal(instance)">
                  <icon-type name="trash" />
                </button>
              </div>
            </td>
          </tbody>
        </table>

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
              <button class="btn btn-error" @click="removeProblem">
                Delete<span v-if="isRemoving" class="loading loading-bars loading-xs"></span>
              </button>
            </div>
          </div>
        </dialog>
      </div>
    </main>
  </vrp-page-layout>
</template>
