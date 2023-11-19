<script lang="ts" setup>
import { useColorMode } from "@vueuse/core";
import { localThemes, modes } from "./themes";
import IconType from "./components/IconType.vue";

const mode = useColorMode({
  attribute: "data-theme",
  modes: modes,
});
</script>

<template>
  <header class="navbar bg-base-300">
    <div class="flex-1">
      <a class="btn btn-ghost normal-case text-xl">Opta Router</a>
      <router-link to="/" class="btn btn-ghost normal-case text-l">Home</router-link>
    </div>
    <div class="justify-end space-x-4">
      <a href="https://github.com/pintowar/opta-router">
        <icon-type name="github" />
      </a>
      <select v-model="mode" class="select w-full max-w-xs" data-choose-theme>
        <option v-for="theme in localThemes" :key="theme" :value="theme">{{ theme }}</option>
      </select>
    </div>
  </header>

  <router-view></router-view>

  <footer class="footer items-center p-4 bg-neutral text-neutral-content fixed bottom-0">
    <div class="items-center grid-flow-col">
      <icon-type name="hash" />
      <p>Copyright © 2023 - All right reserved</p>
    </div>
  </footer>
</template>
