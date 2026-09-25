<template>
  <div class="TwoArchitecture">
    <!-- 按钮部分 -->
    <div class="second-nav-div">
      <!-- <button
        class="btn second-nav-button"
        :class="{ secondActive: buttonIndex === 2 }"
        @click="navButtonClick(2)"
      >
        代码质量检查
      </button> -->
      <button
        class="btn second-nav-button"
        :class="{ secondActive: buttonIndex === 0 }"
        @click="navButtonClick(0)"
      >
        通用反模式检测
      </button>
      <button
        class="btn second-nav-button"
        :class="{ secondActive: buttonIndex === 1 }"
        @click="navButtonClick(1)"
      >
        自定义反模式检测
      </button>
    </div>

    <!-- 主内容部分 -->
    <div class="second-main">
      <!-- <Pmd v-if="buttonIndex === 2" v-on:close="onButtonIndex" /> -->
      <GapDetectorVue v-if="buttonIndex === 0" v-on:close="onButtonIndex" />
      <Architecture v-if="buttonIndex === 1" v-on:close="onButtonIndex" />
    </div>
  </div>
</template>

<script>
// import Pmd from '@/components/Pmd.vue'
import GapDetectorVue from './GapDetector.vue'
import Architecture from './Architecture.vue'

export default {
  name: 'TwoArchitecture',
  components: { GapDetectorVue, Architecture },
  data() {
    return {
      buttonIndex: 0,
      link: '',
    }
  },
  methods: {
    onButtonIndex(url) {
      this.link = url
      if (this.link.type === 'GapDetectorVue') {
        this.navButtonClick(0)
      } else if (this.link.type === 'Architecture') {
        this.navButtonClick(1)
      }
    },
    navButtonClick(index) {
      this.buttonIndex = index
    },
  },
}
</script>

<style scoped>
/* 整体页面样式 */
.TwoArchitecture {
  height: 100vh;
  width: 100%;
  display: inline-block;
}

/* 按钮部分样式 */
.second-nav-div {
  display: flex;
  justify-content: center;
  gap: 300px;
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
