import { vi } from "vitest";
import { ref } from "vue";

export const getMockUseCrud = <T extends { id: number }>(options = {}) => {
  const defaults = {
    isFetching: ref(false),
    page: ref({
      content: [],
      number: 0,
      size: 10,
      totalElements: 0,
      totalPages: 1,
    }),
    error: ref<Error | null>(null),
    fetch: vi.fn(),
    selected: ref<T | null>(null),
    openInsert: ref<boolean>(false),
    isInserting: ref<boolean>(false),
    insertError: ref(null),
    insert: vi.fn(),
    successInsert: ref<boolean>(false),
    openRemove: ref<boolean>(false),
    removeUrl: ref<string>(""),
    removeError: ref<boolean>(false),
    isEditing: ref<boolean>(false),
    isUpdating: ref<boolean>(false),
    updateError: ref(null),
    update: vi.fn(),
    successUpdate: ref<boolean>(false),
    showDeleteModal: vi.fn(),
    updateItem: vi.fn(),
    editItem: vi.fn(),
    errorClose: vi.fn(),
    successClose: vi.fn(),
    toogleInsert: vi.fn(),
    insertItem: vi.fn(),
  };

  return { ...defaults, ...options };
};
