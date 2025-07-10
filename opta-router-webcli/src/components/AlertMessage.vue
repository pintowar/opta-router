<script lang="ts" setup>
import { TransitionPresets, useTimeoutFn, useTransition } from "@vueuse/core";
import { ref, toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    message: string;
    variant?: "info" | "success" | "warning" | "error";
    closable?: boolean;
  }>(),
  {
    variant: "info",
    closable: true,
  }
);

const emit = defineEmits<{
  (e: "close"): void;
}>();

const { message, variant } = toRefs(props);

const isOpen = ref(true);
const source = ref(1);

const opacity = useTransition(source, {
  duration: 2000,
  transition: TransitionPresets.easeInOutCubic,
  onFinished() {
    closeAlert();
  },
});

useTimeoutFn(() => (source.value = 0), 2000, { immediate: true });

const icons = new Map<"info" | "success" | "warning" | "error", string>([
  ["info", "md-info-outlined"],
  ["success", "md-checkcircle-outlined"],
  ["warning", "md-warningamber-round"],
  ["error", "md-cancel-outlined"],
]);
const alerts = new Map<"info" | "success" | "warning" | "error", string>([
  ["info", "alert-info"],
  ["success", "alert-success"],
  ["warning", "alert-warning"],
  ["error", "alert-error"],
]);

function closeAlert() {
  isOpen.value = false;
  emit("close");
}
</script>

<template>
  <div v-if="isOpen" role="alert" :class="`alert fixed ${alerts.get(variant)}`" :style="{ opacity }">
    <v-icon :name="icons.get(variant)" />
    <div>
      <h3 class="font-bold capitalize">{{ variant }}</h3>
      <div class="text-xs">{{ message }}</div>
    </div>
    <button v-if="closable" class="btn btn-xs btn-circle" @click="closeAlert()">X</button>
  </div>
</template>

<style>
.alert {
  padding-top: 6px;
  padding-bottom: 6px;
  z-index: 1001;
  width: 98%;
}
</style>
