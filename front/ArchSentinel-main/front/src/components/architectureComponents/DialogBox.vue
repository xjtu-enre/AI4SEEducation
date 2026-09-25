<template>
    <div v-if="visible" class="dialog-overlay" @click.self="close">
      <div class="dialog-box">
        <header class="dialog-header">
          <slot name="header">Default Header</slot>
        </header>
        <main class="dialog-content">
          <slot name="content">Default Content</slot>
        </main>
        <footer class="dialog-footer">
          <slot name="footer">
            <!-- <button @click="close">Close</button> -->
          </slot>
        </footer>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Dialog',
    props: {
      visible: {
        type: Boolean,
        required: true,
      },
    },
    methods: {
      close() {
        this.$emit('update:visible', false);
      },
    },
  };
  </script>
  
  <style>
  .dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }
  .dialog-box {
    background: white;
    padding: 20px;
    border-radius: 8px;
    min-width: 400px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  .dialog-header,
  .dialog-footer {
    margin-bottom: 10px;
  }
  </style>
  