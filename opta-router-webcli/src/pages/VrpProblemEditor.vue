<script setup lang="ts">
import VrpPageLayout from "../layout/VrpPageLayout.vue";
import { ref } from "vue";
import { VrpProblem } from "../api";

const problem = ref<VrpProblem | null>(null);
</script>

<template>
  <vrp-page-layout :is-fetching="false" :error="false">
    <h1>Zaz</h1>
  </vrp-page-layout>
</template>
