<script lang="ts" setup>
import { themeChange } from "theme-change";
import { ref, onMounted } from "vue";

const themes = ["black", "cupcake", "dark", "dracula", "light", "pastel"];
const selectedTheme = ref(themes[0]);

onMounted(() => themeChange(true));
</script>

<template>
  <header class="navbar bg-base-300">
    <div class="flex-1">
      <a class="btn btn-ghost normal-case text-xl">Opta Router</a>
      <router-link to="/" class="btn btn-ghost normal-case text-l">Home</router-link>
    </div>
    <div class="justify-end space-x-4">
      <a href="https://github.com/pintowar/opta-router">
        <svg
          aria-hidden="true"
          height="32"
          width="32"
          viewBox="0 0 16 16"
          version="1.1"
          data-view-component="true"
          class="fill-current"
        >
          <path
            d="M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 3.58-8 8-8Z"
          ></path>
        </svg>
      </a>
      <select v-model="selectedTheme" class="select w-full max-w-xs" data-choose-theme>
        <option v-for="theme in themes" :key="theme" :value="theme">{{ theme }}</option>
      </select>
    </div>
  </header>

  <router-view></router-view>

  <footer class="footer items-center p-4 bg-neutral text-neutral-content fixed bottom-0">
    <div class="items-center grid-flow-col">
      <svg
        width="36"
        height="36"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
        fill-rule="evenodd"
        clip-rule="evenodd"
        class="fill-current"
      >
        <path
          d="M22.672 15.226l-2.432.811.841 2.515c.33 1.019-.209 2.127-1.23 2.456-1.15.325-2.148-.321-2.463-1.226l-.84-2.518-5.013 1.677.84 2.517c.391 1.203-.434 2.542-1.831 2.542-.88 0-1.601-.564-1.86-1.314l-.842-2.516-2.431.809c-1.135.328-2.145-.317-2.463-1.229-.329-1.018.211-2.127 1.231-2.456l2.432-.809-1.621-4.823-2.432.808c-1.355.384-2.558-.59-2.558-1.839 0-.817.509-1.582 1.327-1.846l2.433-.809-.842-2.515c-.33-1.02.211-2.129 1.232-2.458 1.02-.329 2.13.209 2.461 1.229l.842 2.515 5.011-1.677-.839-2.517c-.403-1.238.484-2.553 1.843-2.553.819 0 1.585.509 1.85 1.326l.841 2.517 2.431-.81c1.02-.33 2.131.211 2.461 1.229.332 1.018-.21 2.126-1.23 2.456l-2.433.809 1.622 4.823 2.433-.809c1.242-.401 2.557.484 2.557 1.838 0 .819-.51 1.583-1.328 1.847m-8.992-6.428l-5.01 1.675 1.619 4.828 5.011-1.674-1.62-4.829z"
        ></path>
      </svg>
      <p>Copyright © 2023 - All right reserved</p>
    </div>
  </footer>
</template>
