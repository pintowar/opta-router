import { mount } from "@vue/test-utils";
import { addIcons } from "oh-vue-icons";
import { BiPlus } from "oh-vue-icons/icons";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import * as api from "../../../api";
import CrudActionButtons from "../../../components/CrudActionButtons.vue";
import PaginatedTable from "../../../components/PaginatedTable.vue";
import { useCrud } from "../../../composables/useCrud";
import VrpCrudPageLayout from "../../../layout/VrpCrudPageLayout.vue";
import VrpLocationForm from "../VrpLocationForm.vue";
import VrpLocations from "../VrpLocations.vue";

addIcons(BiPlus);

vi.mock("../../../composables/useCrud", () => {
  const mockPage = {
    content: [
      { id: 1, name: "Depot A", lat: 0, lng: 0 },
      { id: 2, name: "Customer B", lat: 1, lng: 1, demand: 10 },
    ],
    number: 0,
    size: 10,
    totalElements: 2,
    totalPages: 1,
  };

  return {
    useCrud: vi.fn(() => ({
      isFetching: false,
      page: mockPage,
      error: null,
      fetch: vi.fn(),
      selected: null,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: false,
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    })),
  };
});

vi.mock("@vueuse/core", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
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

vi.spyOn(api, "isDepot").mockImplementation((location) => !("demand" in location));

describe("VrpLocations.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (useCrud as vi.Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [
          { id: 1, name: "Depot A", lat: 0, lng: 0 },
          { id: 2, name: "Customer B", lat: 1, lng: 1, demand: 10 },
        ],
        number: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
      },
      error: null,
      fetch: vi.fn(),
      selected: null,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: false,
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });
  });

  it("renders the title and table when not inserting", async () => {
    const wrapper = mount(VrpLocations, {
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

    expect(wrapper.findComponent({ name: "VrpCrudPageLayout" }).props("title")).toBe("Locations");
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "VrpLocationForm" }).exists()).toBe(false);
  });

  it("displays location data in the table", async () => {
    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    expect(wrapper.text()).toContain("Depot A");
    expect(wrapper.text()).toContain("Depot");
    expect(wrapper.text()).toContain("Customer B");
    expect(wrapper.text()).toContain("10");
    expect(wrapper.text()).toContain("Customer");
  });

  it("shows the VrpLocationForm when openInsert is true", async () => {
    (useCrud as vi.Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: { id: -1, name: "", lat: 0, lng: 0, demand: 0 },
      openInsert: true,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: false,
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    expect(wrapper.findComponent({ name: "VrpLocationForm" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(false);
  });

  it("calls toogleInsert when VrpLocationForm emits close", async () => {
    const mockToogleInsert = vi.fn();
    (useCrud as vi.Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: { id: -1, name: "", lat: 0, lng: 0, demand: 0 },
      openInsert: true,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: false,
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: mockToogleInsert,
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpLocationForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("close");
    expect(mockToogleInsert).toHaveBeenCalledTimes(1);
  });

  it("calls insertItem when VrpLocationForm emits execute", async () => {
    const mockInsertItem = vi.fn();
    const mockSelected = { id: -1, name: "New Location", lat: 0, lng: 0, demand: 50 };
    (useCrud as vi.Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: mockSelected,
      openInsert: true,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: false,
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: mockInsertItem,
    });

    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpLocationForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("execute");
    expect(mockInsertItem).toHaveBeenCalledWith(mockSelected);
  });

  it("calls editItem when CrudActionButtons emits edit", async () => {
    const mockEditItem = vi.fn();
    (useCrud as vi.Mock).mockReturnValue({
      ...useCrud(),
      editItem: mockEditItem,
    });

    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("edit");
    expect(mockEditItem).toHaveBeenCalledWith(expect.any(Object));
  });

  it("calls showDeleteModal when CrudActionButtons emits delete", async () => {
    const mockShowDeleteModal = vi.fn();
    (useCrud as vi.Mock).mockReturnValue({
      ...useCrud(),
      showDeleteModal: mockShowDeleteModal,
    });

    const wrapper = mount(VrpLocations, {
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
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("delete");
    expect(mockShowDeleteModal).toHaveBeenCalledWith(expect.any(Object));
  });
});
