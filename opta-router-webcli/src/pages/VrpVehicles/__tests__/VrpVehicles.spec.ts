import { mount } from "@vue/test-utils";
import { useFetch } from "@vueuse/core";
import { addIcons } from "oh-vue-icons";
import { BiPlus } from "oh-vue-icons/icons"; // Import a dummy icon
import type { Mock } from "vitest";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import CrudActionButtons from "../../../components/CrudActionButtons.vue";
import PaginatedTable from "../../../components/PaginatedTable.vue";
import { useCrud } from "../../../composables/useCrud";
import VrpCrudPageLayout from "../../../layout/VrpCrudPageLayout.vue";
import VrpVehicleForm from "../VrpVehicleForm.vue";
import VrpVehicles from "../VrpVehicles.vue";

addIcons(BiPlus); // Add the dummy icon

// Mock the useCrud composable
vi.mock("../../../composables/useCrud", () => {
  const mockPage = {
    content: [
      { id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } },
      { id: 2, name: "Vehicle B", capacity: 200, depot: { id: 2, name: "Depot B", lat: 0, lng: 0 } },
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
      removeError: false, // Initialize to false
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

// Mock useFetch from @vueuse/core
vi.mock("@vueuse/core", async () => {
  const mockFetchReturn = (initialData: any = {}) => ({
    data: ref(initialData),
    json: vi.fn().mockReturnThis(),
    get: vi.fn().mockReturnThis(),
    delete: vi.fn().mockReturnThis(),
    post: vi.fn().mockReturnThis(),
    put: vi.fn().mockReturnThis(),
    then: vi.fn().mockReturnThis(),
    catch: vi.fn().mockReturnThis(),
  });

  const useFetch = vi.fn((url: string, options?: any) => {
    const mockDepots = [
      { id: 1, name: "Depot A", lat: 0, lng: 0 },
      { id: 2, name: "Depot B", lat: 0, lng: 0 },
    ];
    if (url.includes("depot")) {
      return mockFetchReturn(options?.initialData || mockDepots);
    }
    return mockFetchReturn(options?.initialData);
  });

  return {
    useFetch,
    useTransition: vi.fn(() => ({ value: 0 })),
    TransitionPresets: { easeInOutCubic: vi.fn() },
    useTimeoutFn: vi.fn(() => ({ isActive: ref(false), start: vi.fn(), stop: vi.fn() })),
  };
});

// Mock vue-router
vi.mock("vue-router", () => ({
  useRoute: vi.fn(() => ({
    query: {},
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

describe("VrpVehicles.vue", () => {
  beforeEach(() => {
    // Reset mocks before each test
    vi.clearAllMocks();
    // Re-mock useCrud to ensure a fresh state for each test
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [
          { id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } },
          { id: 2, name: "Vehicle B", capacity: 200, depot: { id: 2, name: "Depot B", lat: 0, lng: 0 } },
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
      removeError: false, // Initialize to false
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
    const mockDepots = [
      { id: 1, name: "Depot A", lat: 0, lng: 0 },
      { id: 2, name: "Depot B", lat: 0, lng: 0 },
    ];
    const createMockResponse = (dataValue: any) => ({
      data: ref(dataValue),
      json: vi.fn(() => createMockResponse(dataValue)),
      then: vi.fn((cb: any) => {
        cb({});
        return createMockResponse(dataValue);
      }),
      catch: vi.fn(() => createMockResponse(dataValue)),
    });
    (useFetch as Mock).mockImplementation((url: string, options?: any) => {
      let initialData: any = {};
      if (url.includes("depot")) {
        initialData = options?.initialData || mockDepots;
      }
      return {
        get: vi.fn(() => createMockResponse(initialData)),
        delete: vi.fn(() => createMockResponse(initialData)),
        post: vi.fn(() => createMockResponse(initialData)),
        put: vi.fn(() => createMockResponse(initialData)),
      };
    });
  });

  it("renders the title and table when not inserting", async () => {
    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });

    expect(wrapper.findComponent({ name: "VrpCrudPageLayout" }).props("title")).toBe("Vehicles");
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "VrpVehicleForm" }).exists()).toBe(false);
  });

  it("displays vehicle data in the table", async () => {
    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick(); // Wait for component to render with mocked data

    expect(wrapper.text()).toContain("Vehicle A");
    expect(wrapper.text()).toContain("100");
    expect(wrapper.text()).toContain("Depot A");
    expect(wrapper.text()).toContain("Vehicle B");
    expect(wrapper.text()).toContain("200");
    expect(wrapper.text()).toContain("Depot B");
  });

  it("shows the VrpVehicleForm when openInsert is true", async () => {
    // Mock useCrud to simulate openInsert state
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: { id: -1, name: "", capacity: 0, depot: { id: -1, name: "", lat: 0, lng: 0 } }, // selected must be set for form to render
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    expect(wrapper.findComponent({ name: "VrpVehicleForm" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "PaginatedTable" }).exists()).toBe(false);
  });

  it("calls toogleInsert when VrpVehicleForm emits close", async () => {
    const mockToogleInsert = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: { id: -1, name: "", capacity: 0, depot: { id: -1, name: "", lat: 0, lng: 0 } },
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpVehicleForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("close");
    expect(mockToogleInsert).toHaveBeenCalledTimes(1);
  });

  it("calls insertItem when VrpVehicleForm emits execute", async () => {
    const mockInsertItem = vi.fn();
    const mockSelected = {
      id: -1,
      name: "New Vehicle",
      capacity: 50,
      depot: { id: 1, name: "Depot A", lat: 0, lng: 0 },
    };
    (useCrud as Mock).mockReturnValue({
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const form = wrapper.findComponent({ name: "VrpVehicleForm" });
    expect(form.exists()).toBe(true);

    await form.vm.$emit("execute");
    expect(mockInsertItem).toHaveBeenCalledWith(mockSelected);
  });

  it("calls editItem when CrudActionButtons emits edit", async () => {
    const mockEditItem = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [{ id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } }],
        number: 0,
        size: 10,
        totalElements: 1,
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
      editItem: mockEditItem,
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("edit");
    expect(mockEditItem).toHaveBeenCalledWith(expect.any(Object)); // Expects the row object
  });

  it("calls showDeleteModal when CrudActionButtons emits delete", async () => {
    const mockShowDeleteModal = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [{ id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } }],
        number: 0,
        size: 10,
        totalElements: 1,
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
      showDeleteModal: mockShowDeleteModal,
      updateItem: vi.fn(),
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);

    await crudActionButtons.vm.$emit("delete");
    expect(mockShowDeleteModal).toHaveBeenCalledWith(expect.any(Object)); // Expects the row object
  });

  it("calls updateItem when CrudActionButtons emits update in edit mode", async () => {
    const mockUpdateItem = vi.fn();
    const mockSelected = {
      id: 1,
      name: "Updated Vehicle",
      capacity: 150,
      depot: { id: 1, name: "Depot A", lat: 0, lng: 0 },
    };
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [{ id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } }],
        number: 0,
        size: 10,
        totalElements: 1,
        totalPages: 1,
      },
      error: null,
      fetch: vi.fn(),
      selected: mockSelected,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: true, // Simulate edit mode
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: mockUpdateItem,
      editItem: vi.fn(),
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);
    expect(crudActionButtons.props("isEditing")).toBe(true);

    await crudActionButtons.vm.$emit("update");
    expect(mockUpdateItem).toHaveBeenCalledWith(mockSelected);
  });

  it("calls editItem with null when CrudActionButtons emits cancel in edit mode", async () => {
    const mockEditItem = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: {
        content: [{ id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } }],
        number: 0,
        size: 10,
        totalElements: 1,
        totalPages: 1,
      },
      error: null,
      fetch: vi.fn(),
      selected: { id: 1, name: "Vehicle A", capacity: 100, depot: { id: 1, name: "Depot A", lat: 0, lng: 0 } },
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: false,
      removeUrl: "",
      removeError: false,
      isEditing: true, // Simulate edit mode
      isUpdating: false,
      updateError: null,
      successUpdate: false,
      showDeleteModal: vi.fn(),
      updateItem: vi.fn(),
      editItem: mockEditItem,
      errorClose: vi.fn(),
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const crudActionButtons = wrapper.findComponent({ name: "CrudActionButtons" });
    expect(crudActionButtons.exists()).toBe(true);
    expect(crudActionButtons.props("isEditing")).toBe(true);

    await crudActionButtons.vm.$emit("cancel");
    expect(mockEditItem).toHaveBeenCalledWith(null);
  });

  it("calls errorClose when VrpCrudPageLayout emits close-error", async () => {
    const mockErrorClose = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: new Error("Some error"), // Simulate an error object
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
      errorClose: mockErrorClose,
      successClose: vi.fn(),
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    expect(layout.exists()).toBe(true);

    await layout.vm.$emit("close-error");
    expect(mockErrorClose).toHaveBeenCalledTimes(1);
  });

  it("calls successClose when VrpCrudPageLayout emits close-success", async () => {
    const mockSuccessClose = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: null,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: true, // Simulate success
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
      successClose: mockSuccessClose,
      toogleInsert: vi.fn(),
      insertItem: vi.fn(),
    });

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    expect(layout.exists()).toBe(true);

    await layout.vm.$emit("close-success");
    expect(mockSuccessClose).toHaveBeenCalledTimes(1);
  });

  it("calls fetch when VrpCrudPageLayout emits fetch", async () => {
    const mockFetch = vi.fn();
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: mockFetch,
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    expect(layout.exists()).toBe(true);

    await layout.vm.$emit("fetch");
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it("updates openRemove when VrpCrudPageLayout emits update:open-remove", async () => {
    const openRemove = ref(false);
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: null,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove,
      removeUrl: "",
      removeError: ref(false),
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    expect(layout.exists()).toBe(true);
    expect(openRemove.value).toBe(false);

    await layout.vm.$emit("update:open-remove", true);
    expect(openRemove.value).toBe(true);

    await layout.vm.$emit("update:open-remove", false);
    expect(openRemove.value).toBe(false);
  });

  it("sets removeError to true when VrpCrudPageLayout emits fail-remove", async () => {
    const removeError = ref(false);
    (useCrud as Mock).mockReturnValue({
      isFetching: false,
      page: { content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 },
      error: null,
      fetch: vi.fn(),
      selected: null,
      openInsert: false,
      isInserting: false,
      insertError: null,
      successInsert: false,
      openRemove: ref(false),
      removeUrl: "",
      removeError, // Initial state
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

    const wrapper = mount(VrpVehicles, {
      global: {
        stubs: {
          "v-icon": true,
          VrpCrudPageLayout,
          PaginatedTable,
          VrpVehicleForm,
          CrudActionButtons,
          DeleteDialog: {
            template: '<div ref="modalRef"></div>',
            setup() {
              return {
                modalRef: ref({
                  showModal: vi.fn(),
                  close: vi.fn(),
                }),
              };
            },
          },
        },
      },
    });
    await nextTick();

    const layout = wrapper.findComponent({ name: "VrpCrudPageLayout" });
    expect(layout.exists()).toBe(true);
    expect(removeError.value).toBe(false);

    await layout.vm.$emit("fail-remove");
    expect(removeError.value).toBe(true);
  });
});
