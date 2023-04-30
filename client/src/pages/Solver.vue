<script setup>
import Dashboard from "../components/Dashboard.vue";
</script>

<template>
  <suspense>
    <template #default>
      <main>
        <dashboard />
      </main>
    </template>
    <template #fallback>
      <main class="flex items-center justify-center h-full">
        <div class="mt-32">
          <button class="btn btn-ghost loading">Loading</button>
        </div>
      </main>
    </template>
  </suspense>
</template>
