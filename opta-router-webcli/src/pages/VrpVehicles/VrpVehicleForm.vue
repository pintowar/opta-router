<script lang="ts" setup>
import { computed, toRefs } from "vue";
import type { Depot, Vehicle } from "../../api";

const props = defineProps<{
  vehicle: Vehicle;
  isLoading: boolean;
  depots: Depot[];
}>();

const emit = defineEmits<{
  (e: "execute"): void;
  (e: "close"): void;
  (e: "update:vehicle", val: Vehicle): void;
}>();

const { depots, isLoading, vehicle } = toRefs(props);

const componentVehicle = computed({
  get: () => vehicle?.value,
  set: (val) => emit("update:vehicle", val),
});
</script>

<template>
  <div v-if="componentVehicle" class="flex flex-col space-y-4">
    <div class="flex flex-row space-x-2">
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="name">Name</label>
        <input
          id="name"
          v-model="componentVehicle.name"
          name="name"
          :disabled="isLoading"
          class="input input-sm input-bordered"
        />
      </div>
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="capacity">Capacity</label>
        <input
          id="capacity"
          v-model="componentVehicle.capacity"
          name="capacity"
          :disabled="isLoading"
          class="input input-sm input-bordered"
        />
      </div>
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="depot">Depot</label>
        <select v-model="componentVehicle.depot" class="select select-bordered select-sm">
          <option v-for="depot in depots" :key="depot.id" :value="depot">
            {{ depot.name }}
          </option>
        </select>
      </div>
    </div>
    <div class="flex flex-row justify-end space-x-2">
      <button type="button" class="btn btn-sm" @click="$emit('close')">Close</button>
      <button type="button" :disabled="isLoading" class="btn btn-sm btn-success" @click="$emit('execute')">
        Save <span v-if="isLoading" class="loading loading-bars loading-xs"></span>
      </button>
    </div>
  </div>
</template>
