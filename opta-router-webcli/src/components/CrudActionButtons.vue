<script lang="ts" setup>
defineProps<{
  isUpdating?: boolean;
  isEditing?: boolean;
}>();

defineEmits<{
  (e: "edit"): void;
  (e: "delete"): void;
  (e: "update"): void;
  (e: "cancel"): void;
}>();
</script>
<template>
  <td class="space-x-2">
    <div v-if="!isEditing">
      <div class="tooltip" data-tip="Edit">
        <button class="btn btn-sm btn-circle" @click="$emit('edit')" data-testid="edit-button">
          <v-icon name="md-edit-twotone" />
        </button>
      </div>
      <div class="tooltip" data-tip="Delete">
        <button class="btn btn-sm btn-circle" @click="$emit('delete')" data-testid="delete-button">
          <v-icon name="md-deleteoutline" />
        </button>
      </div>
    </div>
    <div v-else>
      <div class="tooltip" data-tip="Update">
        <button
          :disabled="isUpdating"
          class="btn btn-sm btn-circle"
          @click="$emit('update')"
          data-testid="update-button"
        >
          <v-icon v-if="!isUpdating" name="md-check" />
          <span v-else class="loading loading-bars loading-xs"></span>
        </button>
      </div>
      <div class="tooltip" data-tip="Cancel">
        <button class="btn btn-sm btn-circle" @click="$emit('cancel')" data-testid="cancel-button">
          <v-icon name="md-close" />
        </button>
      </div>
    </div>
  </td>
</template>
