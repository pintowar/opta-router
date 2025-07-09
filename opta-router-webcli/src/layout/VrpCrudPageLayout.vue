<script lang="ts" setup>
import { useRoute } from "vue-router";
import { VrpPageLayout } from ".";
import { AlertMessage, DeleteDialog, InputSearch } from "../components";

defineProps<{
  isFetching: boolean;
  error: Error | null;
  removeError: boolean;
  updateError: Error | null;
  insertError: Error | null;
  successUpdate: boolean;
  successInsert: boolean;
  removeUrl: string;
  openRemove: boolean;
  selected: { id: number; name: string } | null;
  openInsert: boolean;
  title: string;
}>();

const emit = defineEmits<{
  (e: "close-error"): void;
  (e: "close-success"): void;
  (e: "fetch"): void;
  (e: "fail-remove"): void;
  (e: "toogleInsert"): void;
  (e: "update:open-remove", value: boolean): void;
}>();

const route = useRoute();
</script>
<template>
  <vrp-page-layout :is-fetching="isFetching" :error="error">
    <div class="w-full flex my-2 mx-2 space-x-2">
      <div class="flex flex-col w-full space-y-2">
        <alert-message
          v-if="removeError || updateError || insertError"
          :message="`Could not save/update ${title}`"
          variant="error"
          @close="emit('close-error')"
        />

        <alert-message
          v-if="successUpdate || successInsert"
          :message="`Succcessfully save/update ${title}`"
          variant="success"
          @close="emit('close-success')"
        />

        <delete-dialog
          :url="removeUrl"
          :open="openRemove"
          :message="`Are you sure you want to delete ${selected?.name} (id: ${selected?.id})?`"
          @success-remove="emit('fetch')"
          @fail-remove="emit('fail-remove')"
          @update:open="emit('update:open-remove', $event)"
        />

        <h1 class="text-2xl">{{ title }}</h1>
        <div class="flex w-full justify-between">
          <input-search v-if="!openInsert" :query="`${route.query.q || ''}`" />
          <div v-else></div>
          <button class="btn btn-circle" @click="emit('toogleInsert')">
            <v-icon :name="`${!openInsert ? 'md-add' : 'md-close'}`" />
          </button>
        </div>
        <slot></slot>
      </div>
    </div>
  </vrp-page-layout>
</template>
