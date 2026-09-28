<template>
  <div class="sidebar" v-if="isVisible">
    <ul>
      <li v-for="item in items" :key="item.name">
        <router-link :to="item.path">{{ item.name }}</router-link>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  data() {
    return {
        items: [
        { name: 'Home', path: '/home' },
      ]
    };
  },
  props: {
    isVisible: {
      type: Boolean,
      required: true
    }
  },

  methods: {
    setHeader(header) {
    },
    setContent(items) {
    },
    clearContent() {
      this.items = [];
    }
  }
};
</script>

<style>
.sidebar {
  width: 150px;
  background-color: #f4f4f4;
  padding-left: 10px;
  padding-right: 10px;
  padding-top: 0px;
  padding-bottom: 0px;
  margin: 0px 30px;
  display: flex;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  margin: 10px 0;
}
</style>