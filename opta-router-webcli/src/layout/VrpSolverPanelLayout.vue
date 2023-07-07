<template>
  <div class="flex my-2 mx-2 space-x-2" style="height: calc(100vh - 140px)">
    <div class="flex-initial w-96">
      <slot name="menu"></slot>
    </div>
    <div class="flex-auto">
      <slot name="main"></slot>
    </div>
  </div>
</template>
