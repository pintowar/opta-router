<script lang="ts" setup>
import { defineAsyncComponent } from "vue";

const props = defineProps<{
  name: string;
}>();

// feel free to update this with an svg directory of your choice
const icon = defineAsyncComponent(() => import(`../assets/${props.name}.svg`));
</script>

<template>
  <component :is="icon" class="fill-current" />
</template>
