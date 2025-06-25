<script setup lang="ts">
import { ref, toRefs } from "vue";
import type { Customer } from "../../../api";
import { useFetch } from "@vueuse/core";

const props = defineProps<{
  customers: Customer[];
}>();

const { customers } = toRefs(props);

const isEditing = ref(false);

const emit = defineEmits<{
  (e: "addCustomer", val: Customer): void;
  (e: "removeCustomer", val: Customer): void;
  (e: "changeDemand", val: Customer): void;
}>();

const selectedCustomer = ref<Customer | null>(null);
const { data: allCustomers } = useFetch("/api/vrp-locations/customer").get().json<Customer[]>();

function handleSelectCustomer() {
  if (selectedCustomer.value) {
    emit("addCustomer", selectedCustomer.value);
    selectedCustomer.value = null;
  }
  isEditing.value = false;
}

function handleChangeDemand(customer: Customer) {
  emit("changeDemand", customer);
}
</script>

<template>
  <div class="flex items-center space-x-2 h-8">
    <span>Total customers: {{ customers.length }}</span>
    <div class="flex flex-grow space-x-2 justify-end pr-2">
      <button :disabled="isEditing" class="btn btn-sm btn-circle" @click="isEditing = true">
        <v-icon name="md-add" />
      </button>
      <select v-if="isEditing" v-model="selectedCustomer" class="select select-xs" @change="handleSelectCustomer">
        <option :value="null">None</option>
        <option v-for="cust in allCustomers" :key="cust.id" :value="cust">{{ cust.name }}</option>
      </select>
      <button v-if="isEditing" class="btn btn-sm btn-circle" @click="isEditing = false">
        <v-icon name="md-close" />
      </button>
    </div>
  </div>
  <table class="table table-sm table-zebra w-full">
    <thead>
      <tr>
        <th>Name</th>
        <th>Lat</th>
        <th>Lng</th>
        <th>Demand</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="customer in customers" :key="customer.id">
        <td>{{ customer.name }}</td>
        <td>{{ customer.lat }}</td>
        <td>{{ customer.lng }}</td>
        <td>
          <input
            v-model.number="customer.demand"
            name="demand"
            class="input input-bordered input-xs"
            @change="handleChangeDemand(customer)"
          />
        </td>
        <td>
          <div class="tooltip" data-tip="Remove">
            <button class="btn btn-sm btn-circle" @click="emit('removeCustomer', customer)">
              <v-icon name="md-deleteoutline" />
            </button>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>
