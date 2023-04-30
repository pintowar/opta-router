<script lang="ts" setup>
import JsonEditorVue from "json-editor-vue";
import { ref, toRefs, computed, watch, watchEffect } from "vue";

import { Instance, SolverState } from "../api";

const props = defineProps<{
  extraClass: string;
  instance: Instance | null;
  solverState: SolverState | null;
  isWsConnected: boolean;
}>();

const emit = defineEmits<{
  (e: "onSolve"): void;
  (e: "onTerminate"): void;
  (e: "onDestroy"): void;
  (e: "update:instance", val: Instance | null): void;
  (e: "update:solverState", val: SolverState | null): void;
}>();

const { instance, solverState, isWsConnected, extraClass } = toRefs(props);

const editorContent = ref<Instance | null>(instance.value);
const editorSolverState = ref<SolverState | null>(solverState.value);
const isDetailedPath = ref<boolean>(solverState.value?.detailedPath || false);

const classNames = computed(() => `card bg-base-200 shadow-xl ${extraClass.value}`);
const badgeColor = computed(() => `badge-${isWsConnected.value ? "success" : "error"}`);

watchEffect(() => {
  emit("update:instance", editorContent.value);
});

watch(isDetailedPath, () => {
  const status = solverState.value?.status;
  const newSolverState = status ? { status, detailedPath: isDetailedPath.value } : null;

  emit("update:solverState", newSolverState);
});
</script>

<template>
  <div :class="classNames">
    <div class="card-body">
      <h2 class="card-title">Route Definition</h2>

      <div class="form-control flex flex-row space-x-2">
        <span class="label-text">Show Detailed Path</span>
        <input v-model="isDetailedPath" type="checkbox" class="toggle" />
        <div class="grow align-middle">
          <div class="flex justify-end space-x-2">
            <div>
              <span v-if="editorSolverState?.status" class="badge badge-outline">{{ editorSolverState?.status }}</span>
            </div>
            <div class="tooltip" :data-tip="`Web Socket ${isWsConnected ? 'connected' : 'disconnected'}`">
              <div :class="`badge ${badgeColor}`">WS</div>
            </div>
          </div>
        </div>
      </div>

      <json-editor-vue v-model="editorContent" mode="tree" />

      <div class="card-actions">
        <button class="btn btn-success" @click="$emit('onSolve')">Solve</button>
        <button class="btn btn-warning" @click="$emit('onTerminate')">Terminate</button>
        <button class="btn btn-error" @click="$emit('onDestroy')">Destroy</button>
      </div>
    </div>
  </div>
</template>
