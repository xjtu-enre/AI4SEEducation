<template>
  <div class="Architecture">
    <!-- 按钮部分 -->
    <div class="second-nav-div">
      <button
        class="btn second-nav-button"
        :class="{ secondActive: buttonIndex === 0 }"
        @click="navButtonClick(0)"
      >
        Draw UML
      </button>
      <button
        class="btn second-nav-button"
        :class="{ secondActive: buttonIndex === 1 }"
        @click="navButtonClick(1)"
        style="width: 400px"
      >
        Violation Check Result
      </button>
    </div>

    <div class="second-main">
      <DrawUmlPage v-if="buttonIndex === 0" v-on:close="onButtonIndex" />
      <ViolationInfoPage v-if="buttonIndex === 1" v-on:close="onButtonIndex" />
    </div>
  </div>
</template>

<script>
import DrawUmlPage from './architecturePages/DrawUmlPage'
import ViolationInfoPage from './architecturePages/ViolationInfoPage'

export default {
  name: 'Architecture',
  components: {
    DrawUmlPage,
    ViolationInfoPage,
  },
  data() {
    return {
      buttonIndex: 0,
    }
  },
  methods: {
    navButtonClick(index) {
      this.buttonIndex = index
    },
  },
}
</script>

<style scoped>
/* 整体页面样式 */
.Architecture {
  height: 100%;
  width: 100%;
  display: inline-block;
}

/* 按钮部分样式 */
.second-nav-div {
  display: flex;
  justify-content: center;
  gap: 800px;
  background-color: #f0f0f0;
  padding: 10px 0;
  border-bottom: 1px solid #ccc;
}

.second-nav-button {
  background: none;
  border: none;
  padding: 10px 20px;
  font-size: 1.2rem;
  font-family: 'Arial', sans-serif;
  color: #333;
  cursor: pointer;
  position: relative;
  transition: color 0.3s ease;
  min-width: 120px;
}

.second-nav-button:hover {
  color: #007bff; /* 鼠标悬停变为蓝色 */
}

.secondActive {
  font-weight: bold; /* 激活按钮加粗 */
  color: #333; /* 激活按钮字体颜色为黑色 */
}

.secondActive::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -5px; /* 下划线位置 */
  width: 100%;
  height: 2px; /* 下划线高度 */
  background-color: #000000;
}
</style>
