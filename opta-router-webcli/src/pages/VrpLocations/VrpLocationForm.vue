<script lang="ts" setup>
import { computed, toRefs } from "vue";
import type { Customer, Depot } from "../../api";
import { isDepot } from "../../api";

const props = defineProps<{
  location: Depot | Customer;
  isLoading: boolean;
}>();

const emit = defineEmits<{
  (e: "execute"): void;
  (e: "close"): void;
  (e: "update:location", val: Depot | Customer): void;
}>();

const { isLoading, location } = toRefs(props);

const componentLocation = computed({
  get: () => location?.value,
  set: (val) => emit("update:location", val),
});

const showDepot = computed(() => isDepot(componentLocation.value));

function changeKind(kind: "depot" | "customer") {
  if (kind === "customer") {
    (componentLocation.value as Customer).demand = 0;
  } else {
    componentLocation.value = {
      id: componentLocation.value.id,
      name: componentLocation.value.name,
      lat: componentLocation.value.lat,
      lng: componentLocation.value.lng,
    };
  }
}
</script>

<template>
  <div v-if="componentLocation" class="flex flex-col space-y-4">
    <div class="flex flex-row space-x-2">
      <input type="radio" name="kind" class="radio" :checked="!showDepot" @click="changeKind('customer')" />
      <span>Customer</span>
      <input type="radio" name="kind" class="radio" :checked="showDepot" @click="changeKind('depot')" />
      <span>Depot</span>
    </div>
    <div class="flex flex-row space-x-2">
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="name">Name</label>
        <input
          id="name"
          v-model="componentLocation.name"
          name="name"
          :disabled="isLoading"
          class="input input-bordered input-xs"
        />
      </div>
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="lat">Latitude</label>
        <input
          id="lat"
          v-model.number="componentLocation.lat"
          :disabled="isLoading"
          name="lat"
          class="input input-bordered w-full input-xs"
        />
      </div>
      <div>
        <label class="block tracking-wide text-sm font-bold mb-2" for="lng">Longitude</label>
        <input
          id="lng"
          v-model.number="componentLocation.lng"
          :disabled="isLoading"
          name="lng"
          class="input input-bordered w-full input-xs"
        />
      </div>
      <div v-if="!showDepot">
        <label class="block tracking-wide text-sm font-bold mb-2" for="demand">Demand</label>
        <input
          id="demand"
          v-model.number="(componentLocation as Customer).demand"
          :disabled="isLoading"
          name="demand"
          class="input input-bordered w-full input-xs"
        />
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
