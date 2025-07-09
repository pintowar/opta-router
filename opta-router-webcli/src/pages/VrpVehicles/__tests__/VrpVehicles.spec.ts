import { mount } from "@vue/test-utils";
import { useFetch } from "@vueuse/core";
import { addIcons } from "oh-vue-icons";
import { BiPlus } from "oh-vue-icons/icons";
import type { Mock } from "vitest";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Ref } from "vue";
import { nextTick, ref } from "vue";
import type { Vehicle } from "../../../api";
import CrudActionButtons from "../../../components/CrudActionButtons.vue";
import PaginatedTable from "../../../components/PaginatedTable.vue";
import { getMockUseCrud } from "../../../composables/__tests__/useCrud.mock";
import { useCrud } from "../../../composables/useCrud";
import VrpCrudPageLayout from "../../../layout/VrpCrudPageLayout.vue";
import VrpVehicleForm from "../VrpVehicleForm.vue";
import VrpVehicles from "../VrpVehicles.vue";

addIcons(BiPlus);

vi.mock("../../../composables/useCrud");

vi.mock("@vueuse/core", async () => {
  const actual = await vi.importActual("@vueuse/core");
  return {
    ...actual,
    useFetch: vi.fn(),
    useTimeoutFn: vi.fn(() => ({ isActive: ref(false), start: vi.fn(), stop: vi.fn() })),
  };
});

vi.mock("vue-router", () => ({
  useRoute: vi.fn(() => ({
    query: {},
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

describe("VrpVehicles.vue", () => {
  let mockCrud: ReturnType<typeof getMockUseCrud<Vehicle>>;
  const mockDepots = [
    { id: 1, name: "Depot A", lat: 0, lng: 0 },
    { id: 2, name: "Depot B", lat: 0, lng: 0 },
  ];

  const mockVehicles = [
    { id: 1, name: "Vehicle A", capacity: 100, depot: mockDepots[0] },
    { id: 2, name: "Vehicle B", capacity: 200, depot: mockDepots[1] },
  ];

  beforeEach(() => {
    vi.clearAllMocks();

    mockCrud = getMockUseCrud({
      page: ref({
        content: mockVehicles,
        number: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
      }),
    });
    (useCrud as Mock).mockReturnValue(mockCrud);

    (useFetch as Mock).mockImplementation((url: string | Ref<string>) => {
      const urlValue = typeof url === "string" ? url : url.value;
      if (urlValue.includes("depot")) {
        return {
          get: vi.fn().mockReturnThis(),
          json: vi.fn().mockReturnValue({
            data: ref(mockDepots),
          }),
        };
      }
      return {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({ data: ref(null) }),
      };
    });
  });

  const mountComponent = () => {
    return mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
          },
        },
      },
    });
  };

  it("renders the title and table when not inserting", async () => {
    const wrapper = mountComponent();
    expect(wrapper.findComponent({ name: "VrpCrudPageLayout" }).props("title")).toBe("Vehicles");
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "VrpVehicleForm" }).exists()).toBe(false);
  });

  it("displays vehicle data in the table", async () => {
    const wrapper = mountComponent();
    await nextTick();

    expect(wrapper.text()).toContain("Vehicle A");
    expect(wrapper.text()).toContain("100");
    expect(wrapper.text()).toContain("Depot A");
    expect(wrapper.text()).toContain("Vehicle B");
    expect(wrapper.text()).toContain("200");
    expect(wrapper.text()).toContain("Depot B");
  });

  it("shows the VrpVehicleForm when openInsert is true", async () => {
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = { id: -1, name: "", capacity: 0, depot: mockDepots[0] };
    mockCrud.page.value.content = [];

    const wrapper = mountComponent();
    await nextTick();

    expect(wrapper.findComponent({ name: "VrpVehicleForm" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(false);
  });

  it("calls toogleInsert when VrpVehicleForm emits close", async () => {
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = { id: -1, name: "", capacity: 0, depot: mockDepots[0] };

    const wrapper = mountComponent();
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpVehicleForm" });
    await form.vm.$emit("close");
    expect(mockCrud.toogleInsert).toHaveBeenCalledTimes(1);
  });

  it("calls insertItem when VrpVehicleForm emits execute", async () => {
    const newVehicle = { id: -1, name: "New Vehicle", capacity: 50, depot: mockDepots[0] };
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = newVehicle;

    const wrapper = mountComponent();
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpVehicleForm" });
    await form.vm.$emit("execute");
    expect(mockCrud.insertItem).toHaveBeenCalledWith(newVehicle);
  });

  it("calls editItem when CrudActionButtons emits edit", async () => {
    const wrapper = mountComponent();
    await nextTick();
    const crudButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    await crudButtons.vm.$emit("edit", mockCrud.page.value.content[0]);
    expect(mockCrud.editItem).toHaveBeenCalledWith(mockCrud.page.value.content[0]);
  });

  it("calls showDeleteModal when CrudActionButtons emits delete", async () => {
    const wrapper = mountComponent();
    await nextTick();
    const crudButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    await crudButtons.vm.$emit("delete", mockCrud.page.value.content[0]);
    expect(mockCrud.showDeleteModal).toHaveBeenCalledWith(mockCrud.page.value.content[0]);
  });

  it("calls updateItem when in edit mode and update is emitted", async () => {
    const updatedVehicle = { ...mockVehicles[0], name: "Updated Vehicle" };
    mockCrud.isEditing.value = true;
    mockCrud.selected.value = updatedVehicle;

    const wrapper = mountComponent();
    await nextTick();

    const crudButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    await crudButtons.vm.$emit("update");
    expect(mockCrud.updateItem).toHaveBeenCalledWith(updatedVehicle);
  });

  it("calls editItem with null when in edit mode and cancel is emitted", async () => {
    mockCrud.isEditing.value = true;
    mockCrud.selected.value = mockCrud.page.value.content[0];

    const wrapper = mountComponent();
    await nextTick();

    const crudButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    await crudButtons.vm.$emit("cancel");
    expect(mockCrud.editItem).toHaveBeenCalledWith(null);
  });

  it("calls errorClose when VrpCrudPageLayout emits close-error", async () => {
    mockCrud.error.value = new Error("test error");
    const wrapper = mountComponent();
    await nextTick();
    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    await layout.vm.$emit("close-error");
    expect(mockCrud.errorClose).toHaveBeenCalledTimes(1);
  });

  it("calls successClose when VrpCrudPageLayout emits close-success", async () => {
    mockCrud.successInsert.value = true;
    const wrapper = mountComponent();
    await nextTick();
    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    await layout.vm.$emit("close-success");
    expect(mockCrud.successClose).toHaveBeenCalledTimes(1);
  });

  it("calls fetch when VrpCrudPageLayout emits fetch", async () => {
    const wrapper = mountComponent();
    await nextTick();
    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    await layout.vm.$emit("fetch");
    expect(mockCrud.fetch).toHaveBeenCalledTimes(1);
  });

  it("updates openRemove when VrpCrudPageLayout emits update:open-remove", async () => {
    const wrapper = mountComponent();
    await nextTick();
    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });

    expect(mockCrud.openRemove.value).toBe(false);
    await layout.vm.$emit("update:open-remove", true);
    expect(mockCrud.openRemove.value).toBe(true);
    await layout.vm.$emit("update:open-remove", false);
    expect(mockCrud.openRemove.value).toBe(false);
  });

  it("sets removeError to true when VrpCrudPageLayout emits fail-remove", async () => {
    const wrapper = mountComponent();
    await nextTick();
    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });

    expect(mockCrud.removeError.value).toBe(false);
    await layout.vm.$emit("fail-remove");
    expect(mockCrud.removeError.value).toBe(true);
  });
});
