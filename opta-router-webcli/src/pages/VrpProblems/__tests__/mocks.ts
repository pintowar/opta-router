import { defineComponent } from "vue";

export const VrpPageLayoutMock = defineComponent({
  name: "VrpPageLayout",
  props: ["isFetching", "error"],
  template: `
    <div>
      <div v-if="isFetching">Loading...</div>
      <div v-if="error" role="alert">{{ error.message }}</div>
      <slot />
    </div>
  `,
});

export const VrpProblemFormMock = defineComponent({
  name: "VrpProblemForm",
  props: ["problem", "persistUrl"],
  template: `
    <div>
      <h2>{{ problem.name }}</h2>
      <span>Persist URL: {{ persistUrl }}</span>
    </div>
  `,
});

export const VrpCrudPageLayoutMock = defineComponent({
  name: "VrpCrudPageLayout",
  props: ["isFetching", "error", "removeUrl", "openRemove", "selected"],
  emits: ["toogle-insert", "update:open-remove", "fetch"],
  template: `
    <div>
      <div v-if="isFetching">Loading...</div>
      <div v-if="error" role="alert">{{ error.message }}</div>
      <button @click="$emit('toogle-insert')">New Problem</button>
      <slot />
      <div v-if="openRemove">
        <slot name="delete-dialog">
            <div>Delete dialog for {{ selected?.name }}</div>
            <button @click="$emit('update:open-remove', false)">Cancel</button>
        </slot>
      </div>
    </div>
  `,
});

export const PaginatedTableMock = defineComponent({
  name: "PaginatedTable",
  props: ["page"],
  template: `
        <table>
            <thead>
                <tr><slot name="head" /></tr>
            </thead>
            <tbody>
                <tr v-for="item in page?.content" :key="item.id">
                    <slot name="show" :row="item" />
                </tr>
            </tbody>
        </table>
    `,
});
