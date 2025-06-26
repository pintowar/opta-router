<template>
  <div class="w-full flex flex-row my-2 mx-2 space-x-2">
    <div class="flex w-1/4 min-w-96">
      <slot name="menu"></slot>
    </div>
    <div class="flex-auto flex-shrink-0">
      <slot name="main"></slot>
    </div>
  </div>
</template>
