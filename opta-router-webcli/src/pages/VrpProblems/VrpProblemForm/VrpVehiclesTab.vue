<script setup lang="ts">
import { computed, ref, toRefs } from "vue";
import { useFetch } from "@vueuse/core";
import { uniqBy } from "lodash";
import { Depot, Vehicle } from "../../../api";

const props = defineProps<{
  vehicles: Vehicle[];
}>();

const { vehicles } = toRefs(props);

const emit = defineEmits<{
  (e: "selectValue", val: Vehicle[]): void;
  (e: "removeVehicle", val: Vehicle): void;
  (e: "changeCapacity", val: Vehicle): void;
}>();

const isChangingDepot = ref(false);

const { data: allDepots } = useFetch("/api/vrp-locations/depot").get().json<Depot[]>();

const localDepots = ref<(Depot | null)[]>([]);
const depots = computed<(Depot | null)[]>({
  get() {
    return vehicles.value.length > 0
      ? uniqBy(
          vehicles.value.map((v) => v.depot),
          "id"
        )
      : [null];
  },
  set(deps: (Depot | null)[]) {
    localDepots.value = deps;
  },
});

const selectedDepot = ref<Depot | null>(depots.value.length > 0 ? depots.value[0] : null);

const vehicleUrl = computed(() => `/api/vrp-vehicles/by-depots?ids=${selectedDepot.value?.id}`);
const { data, execute: listVehicles } = useFetch(vehicleUrl, { immediate: false, initialData: [] })
  .get()
  .json<Vehicle[]>();

function handleChangeCapacity(vehicle: Vehicle) {
  emit("changeCapacity", vehicle);
}

async function handleSelectDepot() {
  await listVehicles();
  emit("selectValue", data.value || []);
  depots.value = uniqBy(
    (data.value || []).map((v) => v.depot),
    "id"
  );
  isChangingDepot.value = false;
}
</script>

<template>
  <div v-for="depot in depots" :key="depot?.id">
    <div class="flex items-center space-x-2 h-8">
      <span>Total vehicles: {{ vehicles.length }}</span>

      <div class="flex flex-grow justify-end items-center space-x-2">
        <span>Depot: </span>
        <div v-if="!isChangingDepot" class="flex space-x-2">
          <span> {{ depot?.name || "None" }} ({{ depot?.lat }}, {{ depot?.lng }}) </span>
          <button class="btn btn-circle btn-xs" @click="isChangingDepot = true">
            <v-icon name="md-changecircle" />
          </button>
        </div>
        <select v-else v-model="selectedDepot" class="select select-xs" @change="handleSelectDepot">
          <option v-if="vehicles.length === 0" :key="0" :value="null">None</option>
          <option v-for="depo in allDepots" :key="depo.id" :value="depo">
            {{ depo.name }} ({{ depo.lat }}, {{ depo.lng }})
          </option>
        </select>
      </div>
    </div>

    <table class="table table-sm table-zebra w-full">
      <thead>
        <tr>
          <th>Name</th>
          <th>Capacity</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="vehicle in vehicles.filter((v) => depots.map((it) => it?.id).includes(v.depot.id))"
          :key="vehicle.id"
        >
          <td>{{ vehicle.name }}</td>
          <!-- <td>{{ vehicle.capacity }}</td> -->
          <td>
            <input
              v-model.number="vehicle.capacity"
              name="capacity"
              class="input input-bordered input-xs"
              @change="handleChangeCapacity(vehicle)"
            />
          </td>
          <td>
            <div class="tooltip" data-tip="Remove">
              <button class="btn btn-sm btn-circle" @click="emit('removeVehicle', vehicle)">
                <v-icon name="md-deleteoutline" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
