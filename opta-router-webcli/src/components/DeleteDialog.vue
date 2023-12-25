<script lang="ts" setup>
import { useFetch } from "@vueuse/core";
import { computed, ref, toRefs, watch } from "vue";

const props = defineProps<{
  open: boolean;
  message: string;
  url: string;
}>();

const emit = defineEmits<{
  (e: "update:url", val: string): void;
  (e: "update:open", val: boolean): void;
  (e: "successRemove"): void;
  (e: "failRemove", val: unknown): void;
}>();

const { open, message, url } = toRefs(props);

const modalRef = ref<HTMLDialogElement | null>(null);

const openedModal = computed({
  get: () => open.value,
  set: (val) => emit("update:open", val),
});

const removeUrl = computed({
  get: () => url.value,
  set: (val) => emit("update:url", val),
});

const {
  isFetching: isRemoving,
  error: removeError,
  execute: remove,
} = useFetch(removeUrl, { immediate: false }).delete();

async function removeAction() {
  await remove();
  openedModal.value = false;
  emit("successRemove");
}

watch(openedModal, (opened) => {
  if (opened) {
    modalRef?.value?.showModal();
  } else {
    modalRef?.value?.close();
  }
});

watch(removeError, (error) => {
  if (error) {
    emit("failRemove", error);
  }
});
</script>

<template>
  <dialog id="delete_modal" ref="modalRef" class="modal" @close="openedModal = false">
    <div class="modal-box">
      <form method="dialog">
        <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
      </form>
      <h3 class="font-bold text-lg text-warning">Warning!</h3>
      <p class="py-4">{{ message }}</p>
      <div class="modal-action space-x-2">
        <form method="dialog">
          <button class="btn" @click="openedModal = false">Close</button>
        </form>
        <button :disabled="isRemoving" class="btn btn-error" @click="removeAction">
          Delete <span v-if="isRemoving" class="loading loading-bars loading-xs"></span>
        </button>
      </div>
    </div>
  </dialog>
</template>
