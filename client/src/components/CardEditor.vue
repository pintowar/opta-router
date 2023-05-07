<script lang="ts" setup>
// import JsonEditorVue from "json-editor-vue";
import { toRefs, computed } from "vue";

// import { Instance } from "../api";

const props = defineProps<{
  // instance: Instance | null;
  status: string | null;
  isDetailedPath: boolean;
  isWsConnected: boolean;
}>();

const emit = defineEmits<{
  (e: "onSolve"): void;
  (e: "onTerminate"): void;
  (e: "onDestroy"): void;
  // (e: "update:instance", val: Instance | null): void;
  (e: "update:isDetailedPath", val: boolean): void;
}>();

// instance
const { status, isDetailedPath, isWsConnected } = toRefs(props);

// const editorContent = computed({
//   get: () => instance.value,
//   set: (val) => emit("update:instance", val),
// });

const editorDetailedPath = computed({
  get: () => isDetailedPath.value,
  set: (val) => emit("update:isDetailedPath", val),
});

const badgeColor = computed(() => `badge-${isWsConnected.value ? "success" : "error"}`);
</script>

<template>
  <div class="card bg-base-200 shadow-xl">
    <div class="card-body">
      <h2 class="card-title">Route Definition</h2>

      <div class="form-control flex flex-row space-x-2">
        <span class="label-text">Show Detailed Path</span>
        <input v-model="editorDetailedPath" type="checkbox" class="toggle" />
        <div class="grow align-middle">
          <div class="flex justify-end space-x-2">
            <div>
              <span v-if="status" class="badge badge-outline">{{ status }}</span>
            </div>
            <div class="tooltip" :data-tip="`Web Socket ${isWsConnected ? 'connected' : 'disconnected'}`">
              <div :class="`badge ${badgeColor}`">WS</div>
            </div>
          </div>
        </div>
      </div>

      <!-- <json-editor-vue v-model="editorContent" mode="tree" class="jse-theme-dark" /> -->

      <div class="card-actions">
        <button class="btn btn-sm btn-success" @click="$emit('onSolve')">Solve</button>
        <button class="btn btn-sm btn-warning" @click="$emit('onTerminate')">Terminate</button>
        <button class="btn btn-sm btn-error" @click="$emit('onDestroy')">Clear</button>
      </div>
    </div>
  </div>
</template>

<style>
.jse-contents {
  max-height: 400px;
}

.jse-theme-dark {
  /* over all fonts, sizes, and colors */
  --jse-theme-color: #2f6dd0;
  --jse-theme-color-highlight: #467cd2;
  --jse-background-color: #1e1e1e;
  --jse-text-color: #d4d4d4;

  /* main, menu, modal */
  --jse-main-border: 1px solid #4f4f4f;
  --jse-menu-color: #fff;
  --jse-modal-background: #2f2f2f;
  --jse-modal-overlay-background: rgba(0, 0, 0, 0.5);
  --jse-modal-code-background: #2f2f2f;

  /* panels: navigation bar, gutter, search box */
  --jse-panel-background: #333333;
  --jse-panel-background-border: 1px solid #464646;
  --jse-panel-color: var(--jse-text-color);
  --jse-panel-color-readonly: #737373;
  --jse-panel-border: 1px solid #3c3c3c;
  --jse-panel-button-color-highlight: #e5e5e5;
  --jse-panel-button-background-highlight: #464646;

  /* navigation-bar */
  --jse-navigation-bar-background: #656565;
  --jse-navigation-bar-background-highlight: #7e7e7e;
  --jse-navigation-bar-dropdown-color: var(--jse-text-color);

  /* context menu */
  --jse-context-menu-background: #4b4b4b;
  --jse-context-menu-background-highlight: #595959;
  --jse-context-menu-separator-color: #595959;
  --jse-context-menu-color: var(--jse-text-color);
  --jse-context-menu-button-background: #737373;
  --jse-context-menu-button-background-highlight: #818181;
  --jse-context-menu-button-color: var(--jse-context-menu-color);

  /* contents: json key and values */
  --jse-key-color: #9cdcfe;
  --jse-value-color: var(--jse-text-color);
  --jse-value-color-number: #b5cea8;
  --jse-value-color-boolean: #569cd6;
  --jse-value-color-null: #569cd6;
  --jse-value-color-string: #ce9178;
  --jse-value-color-url: #ce9178;
  --jse-delimiter-color: #949494;
  --jse-edit-outline: 2px solid var(--jse-text-color);

  /* contents: selected or hovered */
  --jse-selection-background-color: #464646;
  --jse-selection-background-light-color: #333333;
  --jse-hover-background-color: #343434;

  /* contents: section of collapsed items in an array */
  --jse-collapsed-items-background-color: #333333;
  --jse-collapsed-items-selected-background-color: #565656;
  --jse-collapsed-items-link-color: #b2b2b2;
  --jse-collapsed-items-link-color-highlight: #ec8477;

  /* contents: highlighting of search results */
  --jse-search-match-color: #724c27;
  --jse-search-match-outline: 1px solid #966535;
  --jse-search-match-active-color: #9f6c39;
  --jse-search-match-active-outline: 1px solid #bb7f43;

  /* contents: inline tags inside the JSON document */
  --jse-tag-background: #444444;
  --jse-tag-color: #bdbdbd;

  /* controls in modals: inputs, buttons, and \`a\` */
  --jse-input-background: #3d3d3d;
  --jse-input-border: var(--jse-main-border);
  --jse-button-background: #808080;
  --jse-button-background-highlight: #7a7a7a;
  --jse-button-color: #e0e0e0;
  --jse-a-color: #55abff;
  --jse-a-color-highlight: #4387c9;

  /* svelte-select */
  --background: #3d3d3d;
  --border: 1px solid #4f4f4f;
  --listBackground: #3d3d3d;
  --itemHoverBG: #505050;
  --multiItemBG: #5b5b5b;
  --inputColor: #d4d4d4;
  --multiClearBG: #8a8a8a;
  --listShadow: 0 2px 6px 0 rgba(0, 0, 0, 0.24);

  /* color picker */
  --jse-color-picker-background: #656565;
  --jse-color-picker-border-box-shadow: #8c8c8c 0 0 0 1px;
}
</style>
