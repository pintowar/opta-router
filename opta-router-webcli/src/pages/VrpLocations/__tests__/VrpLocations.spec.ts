import { mount } from "@vue/test-utils";
import { addIcons } from "oh-vue-icons";
import { BiPlus } from "oh-vue-icons/icons";
import type { Mock } from "vitest";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import type { Customer, Depot } from "../../../api";
import CrudActionButtons from "../../../components/CrudActionButtons.vue";
import PaginatedTable from "../../../components/PaginatedTable.vue";
import { getMockUseCrud } from "../../../composables/__tests__/useCrud.mock";
import { useCrud } from "../../../composables/useCrud";
import VrpCrudPageLayout from "../../../layout/VrpCrudPageLayout.vue";
import VrpLocationForm from "../VrpLocationForm.vue";
import VrpLocations from "../VrpLocations.vue";

addIcons(BiPlus);

vi.mock("../../../composables/useCrud");

vi.mock("@vueuse/core", async () => {
  return {
    useFetch: vi.fn(),
    useTransition: vi.fn(() => ({ value: 0 })),
    TransitionPresets: { easeInOutCubic: vi.fn() },
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

describe("VrpLocations.vue", () => {
  let mockCrud: ReturnType<typeof getMockUseCrud<Customer | Depot>>;

  beforeEach(() => {
    vi.clearAllMocks();
    mockCrud = getMockUseCrud({
      page: ref({
        content: [
          { id: 1, name: "Depot A", lat: 0, lng: 0 },
          { id: 2, name: "Customer B", lat: 1, lng: 1, demand: 10 },
        ],
        number: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
      }),
    });
    (useCrud as Mock).mockReturnValue(mockCrud);
  });

  const mountComponent = () => {
    return mount(VrpLocations, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpLocationForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
          },
          LocationMap: true,
        },
      },
    });
  };

  it("renders the title and table when not inserting", async () => {
    const wrapper = mountComponent();
    expect(wrapper.findComponent({ name: "VrpCrudPageLayout" }).props("title")).toBe("Locations");
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "VrpLocationForm" }).exists()).toBe(false);
  });

  it("displays location data in the table", async () => {
    const wrapper = mountComponent();
    await nextTick();

    expect(wrapper.text()).toContain("Depot A");
    expect(wrapper.text()).toContain("Depot");
    expect(wrapper.text()).toContain("Customer B");
    expect(wrapper.text()).toContain("10");
    expect(wrapper.text()).toContain("Customer");
  });

  it("shows the VrpLocationForm when openInsert is true", async () => {
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = { id: -1, name: "", lat: 0, lng: 0, demand: 0 };
    mockCrud.page.value.content = [];

    const wrapper = mountComponent();
    await nextTick();

    expect(wrapper.findComponent({ name: "VrpLocationForm" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(false);
  });

  it("calls toogleInsert when VrpLocationForm emits close", async () => {
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = { id: -1, name: "", lat: 0, lng: 0, demand: 0 };
    const wrapper = mountComponent();
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpLocationForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("close");
    expect(mockCrud.toogleInsert).toHaveBeenCalledTimes(1);
  });

  it("calls insertItem when VrpLocationForm emits execute", async () => {
    const mockSelected = { id: -1, name: "New Location", lat: 0, lng: 0, demand: 50 };
    mockCrud.openInsert.value = true;
    mockCrud.selected.value = mockSelected;

    const wrapper = mountComponent();
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpLocationForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("execute");
    expect(mockCrud.insertItem).toHaveBeenCalledWith(mockSelected);
  });

  it("calls editItem when CrudActionButtons emits edit", async () => {
    const wrapper = mountComponent();
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("edit");
    expect(mockCrud.editItem).toHaveBeenCalledWith(expect.any(Object));
  });

  it("calls showDeleteModal when CrudActionButtons emits delete", async () => {
    const wrapper = mountComponent();
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("delete");
    expect(mockCrud.showDeleteModal).toHaveBeenCalledWith(expect.any(Object));
  });
});
