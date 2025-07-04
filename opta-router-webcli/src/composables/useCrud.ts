
import type { AfterFetchContext } from '@vueuse/core';
import { useFetch } from '@vueuse/core';
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { Page } from '../api';

export function useCrud<T extends { id: number }>(baseRestUrl: string, initialValue: T) {
  const route = useRoute();

  const url = computed(
    () => `${baseRestUrl}?page=${route.query.page || 0}&size=${route.query.size || 10}&q=${route.query.q || ''}`
  );

  const {
    isFetching,
    data: page,
    error,
    execute: fetch,
  } = useFetch(url, { refetch: true, afterFetch: afterFetchContext }).get().json<Page<T>>();

  const selected = ref<T | null>(null);
  const openInsert = ref<boolean>(false);
  const baseIdRestUrl = computed(() => `${baseRestUrl}/${selected.value?.id}`);

  const insertUrl = `${baseRestUrl}/insert`;
  const {
    isFetching: isInserting,
    error: insertError,
    execute: insert,
    statusCode: insertCode,
  } = useFetch(insertUrl, { immediate: false }).post(selected);
  const successInsert = computed(() => (insertCode.value || 0) >= 200 && (insertCode.value || 0) < 300);

  const openRemove = ref<boolean>(false);
  const removeUrl = computed(() => `${baseIdRestUrl.value}/remove`);
  const removeError = ref(false);

  const isEditing = ref(false);
  const updateUrl = computed(() => `${baseIdRestUrl.value}/update`);
  const {
    isFetching: isUpdating,
    error: updateError,
    execute: update,
    statusCode: updateCode,
  } = useFetch(updateUrl, { immediate: false }).put(selected);
  const successUpdate = computed(() => (updateCode.value || 0) >= 200 && (updateCode.value || 0) < 300);

  function afterFetchContext(ctx: AfterFetchContext) {
    selected.value = null;
    return ctx;
  }

  function showDeleteModal(item: T) {
    isEditing.value = false;
    selected.value = item;
    openRemove.value = true;
  }

  async function updateItem(item: T | null) {
    if (item) {
      await update();
      await fetch();
    }
  }

  function editItem(item: T | null) {
    selected.value = item;
    isEditing.value = item !== null;
    if (item === null) {
      fetch();
    }
  }

  function errorClose() {
    removeError.value = false;
    insertError.value = null;
    updateError.value = null;
  }

  function successClose() {
    updateCode.value = null;
    insertCode.value = null;
  }

  function toogleInsert() {
    if (!openInsert.value) {
      selected.value = initialValue;
    } else {
      selected.value = null;
    }
    openInsert.value = !openInsert.value;
  }

  async function insertItem(item: T | null) {
    if (item) {
      await insert();
      toogleInsert();
      await fetch();
    }
  }

  return {
    isFetching,
    page,
    error,
    fetch,
    selected,
    openInsert,
    isInserting,
    insertError,
    insert,
    successInsert,
    openRemove,
    removeUrl,
    removeError,
    isEditing,
    isUpdating,
    updateError,
    update,
    successUpdate,
    showDeleteModal,
    updateItem,
    editItem,
    errorClose,
    successClose,
    toogleInsert,
    insertItem,
  };
}
