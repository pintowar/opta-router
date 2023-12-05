<script lang="ts" setup>
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

const { message, variant } = toRefs(props);

const isOpen = ref(true);

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
</script>

<template>
  <div v-if="isOpen" role="alert" :class="`alert ${alerts.get(variant)}`">
    <v-icon :name="icons.get(variant)" />
    <div>
      <h3 class="font-bold capitalize">{{ variant }}</h3>
      <div class="text-xs">{{ message }}</div>
    </div>
    <button v-if="closable" class="btn btn-xs btn-circle" @click="isOpen = false">X</button>
  </div>
</template>
