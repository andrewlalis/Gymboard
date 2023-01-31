<!--
The standard wrapper for most content that appears in the app's central view,
which centers content into a column on larger screens, and expands to full
width for smaller screens.

Use this as the root component for any pages you create.
-->
<template>
  <div class="row justify-center">
    <div class="col-xs-12 col-md-6 q-px-sm">
      <slot>
        <p>Page content</p>
      </slot>
    </div>
  </div>
</template>
