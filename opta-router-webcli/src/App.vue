<script lang="ts" setup>
import { useColorMode } from "@vueuse/core";
import { localThemes, modes } from "./helpers/themes";

const mode = useColorMode({
  attribute: "data-theme",
  modes: modes,
});
</script>

<template>
  <div class="flex flex-col h-screen">
    <header class="navbar bg-base-300 p-4 shadow-md z-10">
      <div class="flex-1">
        <a class="btn btn-ghost normal-case text-xl">Opta Router</a>
        <router-link to="/" class="btn btn-ghost normal-case text-l">Home</router-link>
        <router-link to="/locations" class="btn btn-ghost normal-case text-l">Locations</router-link>
        <router-link to="/vehicles" class="btn btn-ghost normal-case text-l">Vehicles</router-link>
      </div>
      <div class="flex items-center justify-end space-x-4">
        <a href="https://github.com/pintowar/opta-router">
          <v-icon name="fa-github" scale="2" />
        </a>
        <select v-model="mode" class="select select-sm w-full max-w-xs" data-choose-theme>
          <option v-for="theme in localThemes" :key="theme" :value="theme">{{ theme }}</option>
        </select>
      </div>
    </header>
    <main class="flex flex-grow overflow-hidden">
      <router-view></router-view>
    </main>
    <footer class="footer items-center p-4 bg-neutral text-neutral-content shadow-md z-10">
      <div class="items-center grid-flow-col">
        <v-icon name="fa-slack-hash" scale="2" />
        <p>Copyright © 2023 - All right reserved</p>
      </div>
    </footer>
  </div>
</template>
