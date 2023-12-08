<script setup>
import { ref, computed } from "vue";
import { Combobox, ComboboxInput, ComboboxOptions, ComboboxOption } from "@headlessui/vue";

const people = ["Durward Reynolds", "Kenton Towne", "Therese Wunsch", "Benedict Kessler", "Katelyn Rohan"];
const selectedPerson = ref(people[0]);
const query = ref("");

const filteredPeople = computed(() =>
  query.value === ""
    ? people
    : people.filter((person) => {
        return person.toLowerCase().includes(query.value.toLowerCase());
      })
);
</script>

<template>
  <Combobox v-model="selectedPerson">
    <ComboboxInput class="input input-bordered w-full input-xs" @change="query = $event.target.value" />
    <ComboboxOptions class="p-2 shadow menu dropdown-content dropdown-end z-[1] bg-base-100 rounded-box w-52">
      <ComboboxOption v-for="person in filteredPeople" :key="person.id" :value="person">
        {{ person }}
      </ComboboxOption>
    </ComboboxOptions>
  </Combobox>
</template>
