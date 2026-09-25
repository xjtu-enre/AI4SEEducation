<template>
  <div class="GapDetector">
    <!-- 按钮部分 -->
    <div class="second-nav-div">
      <button
        class="btn second-nav-button"
        style="width: 13%"
        :class="{ secondActive: buttonIndex === 6 }"
        @click="navButtonClick(6)"
      >
        反模式检测结果
      </button>
      <button
        class="btn second-nav-button"
        style="width: 15%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 0 }"
        @click="navButtonClick(0)"
      >
        接口未解耦<br>
        Abstraction without Decoupling
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 1 }"
        @click="navButtonClick(1)"
      >
        循环依赖<br>
        Cyclic Dependency
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 2 }"
        @click="navButtonClick(2)"
      >
        循环继承<br>
        Cyclic Hierarchy
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 7 }"
        @click="navButtonClick(7)"
      >
        数据泥团<br>
        Data Clumps
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;" 
        :class="{ secondActive: buttonIndex === 3 }"
        @click="navButtonClick(3)"
      >
        特性依恋<br>
        Feature Envy
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 4 }"
        @click="navButtonClick(4)"
      >
        多重路径继承<br>
        Multipath Hierarchy
      </button>
      <button
        class="btn second-nav-button"
        style="width: 13%; display: flex; align-items: center; justify-content: center;"
        :class="{ secondActive: buttonIndex === 5 }"
        @click="navButtonClick(5)"
      >
        霰弹式修改<br>
        Shotgun Surgery
      </button>
    </div>

    <!-- 主内容部分 -->
    <div class="second-main">
      <GapContent v-if="buttonIndex === 6" v-on:close="onButtonIndex" />
      <AWD v-if="buttonIndex === 0" v-on:close="onButtonIndex" />
      <CD v-if="buttonIndex === 1" v-on:close="onButtonIndex" />
      <CH v-if="buttonIndex === 2" v-on:close="onButtonIndex" />
      <DC v-if="buttonIndex === 7" v-on:close="buttonIndex" />
      <FE v-if="buttonIndex === 3" v-on:close="onButtonIndex" />
      <MH v-if="buttonIndex === 4" v-on:close="onButtonIndex" />
      <SS v-if="buttonIndex === 5" v-on:close="onButtonIndex" />
    </div>
  </div>
</template>

<script>
import AWD from '@/components/gapDetector/AWD.vue'
import FE from '@/components/gapDetector/FE.vue'
import CD from '@/components/gapDetector/CD.vue'
import CH from '@/components/gapDetector/CH.vue'
import MH from '@/components/gapDetector/MH.vue'
import SS from '@/components/gapDetector/SS.vue'
import GapContent from '@/components/gapDetector/GapContent.vue'
import DC from '@/components/gapDetector/DC.vue'

export default {
  name: 'GapDetector',
  components: { GapContent, SS, MH, CH, CD, FE, AWD, DC },
  data() {
    return {
      buttonIndex: 6,
      link: '',
    }
  },
  methods: {
    onButtonIndex(url) {
      this.link = url
      if (this.link.type === 'ener-java') {
        this.$emit('close', { type: 'ener-java' })
      } else if (this.link.type === 'AWD') {
        this.navButtonClick(0)
      } else if (this.link.type === 'CD') {
        this.navButtonClick(1)
      } else if (this.link.type === 'CH') {
        this.navButtonClick(2)
      } else if (this.link.type === 'FE') {
        this.navButtonClick(3)
      } else if (this.link.type === 'MH') {
        this.navButtonClick(4)
      } else if (this.link.type === 'SS') {
        this.navButtonClick(5)
      } else if (this.link.type === 'DC') {
        this.navButtonClick(7)
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
.GapDetector {
  height: 92%;
  width: 100%;
  display: inline-block;
}

/* 按钮部分样式 */
.second-nav-div {
  display: flex;
  justify-content: space-between;
  background-color: #f0f0f0; /* 浅灰色背景 */
  padding: 10px 0;
  border-bottom: 1px solid #ccc; /* 添加分割线 */
}

.second-nav-button {
  background: none;
  border: none;
  padding: 10px 0;
  font-size: 1.2rem; /* 增大字体 */
  font-family: 'Arial', sans-serif;
  color: #333;
  cursor: pointer;
  position: relative; /* 使下划线相对定位 */
  transition: color 0.3s ease;
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
