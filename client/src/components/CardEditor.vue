<script lang="ts" setup>
import VueJsoneditor from "vue3-ts-jsoneditor";
import { ref, toRefs, computed, watchEffect } from "vue";

import { belgium } from "../samples";
import { Instance } from "../api";

const props = defineProps<{
  extraClass: string;
  instance: Instance | null;
  detailed: boolean;
  solverStatus: string;
}>();

const emit = defineEmits<{
  (e: 'onSolve'): void,
  (e: 'onTerminate'): void,
  (e: 'onDestroy'): void,
  (e: 'update:instance', val: Instance): void,
  (e: 'update:detailed', val: boolean): void
}>();

const { instance, detailed, solverStatus, extraClass } = toRefs(props);
const editorContent = ref<Instance | null>(instance.value);
const editorDetailed = ref<boolean>(detailed.value);

const classNames = computed(() => `card bg-base-200 shadow-xl ${extraClass.value}`);

watchEffect(() => {
  const editingValue = editorContent?.value;
  emit("update:instance", editingValue);
});

watchEffect(() => {
  emit("update:detailed", editorDetailed.value);
});

function loadSample() {
  editorContent.value = belgium;
}

</script>

<template>
    <div :class="classNames">
      <div class="card-body">
        <h2 class="card-title">Route Definition</h2>
        
        <div class="flex space-x-2">
          <a @click="loadSample" class="link">Load Sample</a>
          <span v-if="solverStatus" class="badge badge-outline">{{ solverStatus }}</span>
        </div>

        <div class="form-control flex-row space-x-2">
          <span class="label-text">Show Detailed Path</span> 
          <input type="checkbox" class="toggle" v-model="editorDetailed" />
        </div>

        <vue-jsoneditor
          height="400"
          mode="tree"
          v-model:json="editorContent"
          :darkTheme="true"
        />

        <div class="card-actions">
          <button @click="$emit('onSolve')" class="btn btn-success">Solve</button>
          <button @click="$emit('onTerminate')" class="btn btn-warning">Terminate</button>
          <button @click="$emit('onDestroy')" class="btn btn-error">Destroy</button>
        </div>
      </div>
    </div>
</template>