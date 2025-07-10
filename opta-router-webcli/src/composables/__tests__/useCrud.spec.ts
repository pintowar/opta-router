import { useFetch } from "@vueuse/core";
import type { Mock } from "vitest";
import { ref } from "vue";
import { useRoute } from "vue-router";
import { useCrud } from "../useCrud";

// Mock vue-router's useRoute
vi.mock("vue-router", () => ({
  useRoute: vi.fn(),
}));

// Mock @vueuse/core's useFetch globally, but without shared refs for statusCode
vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(() => ({
    get: vi.fn(() => ({
      json: vi.fn(() => ({
        isFetching: ref(false),
        data: ref({ content: [], totalPages: 0, totalElements: 0 }),
        error: ref(null),
        execute: vi.fn(),
      })),
    })),
    post: vi.fn(() => ({
      isFetching: ref(false),
      error: ref(null),
      execute: vi.fn(),
      statusCode: ref(200), // Each call gets a new ref
    })),
    put: vi.fn(() => ({
      isFetching: ref(false),
      error: ref(null),
      execute: vi.fn(),
      statusCode: ref(200), // Each call gets a new ref
    })),
  })),
}));

describe("useCrud", () => {
  const baseRestUrl = "/api/test";
  const initialValue = { id: 0, name: "" };

  beforeEach(() => {
    // Reset mocks before each test
    vi.clearAllMocks();
    (useRoute as Mock).mockReturnValue({
      query: ref({ page: 0, size: 10, q: "" }),
    });
  });

  it("should initialize with default values", () => {
    const { isFetching, page, error, selected, openInsert, isEditing } = useCrud(baseRestUrl, initialValue);

    expect(isFetching.value).toBe(false);
    expect(page.value).toEqual({ content: [], totalPages: 0, totalElements: 0 });
    expect(error.value).toBeNull();
    expect(selected.value).toBeNull();
    expect(openInsert.value).toBe(false);
    expect(isEditing.value).toBe(false);
  });

  it("should call fetch on initialization", () => {
    useCrud(baseRestUrl, initialValue);
    expect(useFetch).toHaveBeenCalledWith(expect.any(Object), expect.any(Object));
  });

  it("should set selected item and openRemove when showDeleteModal is called", () => {
    const { selected, openRemove, showDeleteModal } = useCrud(baseRestUrl, initialValue);
    const item = { id: 1, name: "Test Item" };
    showDeleteModal(item);
    expect(selected.value).toEqual(item);
    expect(openRemove.value).toBe(true);
  });

  it("should set selected item and isEditing when editItem is called with an item", () => {
    const { selected, isEditing, editItem } = useCrud(baseRestUrl, initialValue);
    const item = { id: 1, name: "Test Item" };
    editItem(item);
    expect(selected.value).toEqual(item);
    expect(isEditing.value).toBe(true);
  });

  it("should clear selected item and set isEditing to false when editItem is called with null", async () => {
    const { selected, isEditing, editItem, fetch } = useCrud(baseRestUrl, initialValue);
    editItem(null);
    expect(selected.value).toBeNull();
    expect(isEditing.value).toBe(false);
    expect(fetch).toHaveBeenCalled();
  });

  it("should toggle openInsert and set selected to initialValue when opening", () => {
    const { openInsert, selected, toogleInsert } = useCrud(baseRestUrl, initialValue);
    toogleInsert();
    expect(openInsert.value).toBe(true);
    expect(selected.value).toEqual(initialValue);
  });

  it("should toggle openInsert and set selected to null when closing", () => {
    const { openInsert, selected, toogleInsert } = useCrud(baseRestUrl, initialValue);
    toogleInsert(); // Open
    toogleInsert(); // Close
    expect(openInsert.value).toBe(false);
    expect(selected.value).toBeNull();
  });

  it("should call insert and fetch when insertItem is called with an item", async () => {
    const { insert, fetch, insertItem } = useCrud(baseRestUrl, initialValue);
    const item = { id: 1, name: "New Item" };
    await insertItem(item);
    expect(insert).toHaveBeenCalled();
    expect(fetch).toHaveBeenCalled();
  });

  it("should call update and fetch when updateItem is called with an item", async () => {
    const { update, fetch, updateItem } = useCrud(baseRestUrl, initialValue);
    const item = { id: 1, name: "Updated Item" };
    await updateItem(item);
    expect(update).toHaveBeenCalled();
    expect(fetch).toHaveBeenCalled();
  });

  it("should clear errors when errorClose is called", () => {
    const { insertError, updateError, removeError, errorClose } = useCrud(baseRestUrl, initialValue);
    insertError.value = "insert error";
    updateError.value = "update error";
    removeError.value = true;
    errorClose();
    expect(insertError.value).toBeNull();
    expect(updateError.value).toBeNull();
    expect(removeError.value).toBe(false);
  });
});
