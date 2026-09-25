<template>
  <div class="DCouplingAntiPattern">
    <div class="second-nav-div">
      <button
      class="btn second-nav-button intrusive-button"
      style="width: 13%"
      :class="{ secondActive: buttonIndex === 4 }"
      @click="navButtonClick(4)"
    >
      度量结果
    </button>
      <button
        class="btn second-nav-button intrusive-button"
        style="width: 13%"
        :class="{ secondActive: buttonIndex === 3 }"
        @click="navButtonClick(3)"
      >
        耦合面分析
      </button>
      <button
        class="btn second-nav-button intrusive-button"
        style="width: 13%"
        :class="{ secondActive: buttonIndex === 0 }"
        @click="navButtonClick(0)"
      >
        实体归属
      </button>
      <button
        class="btn second-nav-button intrusive-button"
        style="width: 13%"
        :class="{ secondActive: buttonIndex === 1 }"
        @click="navButtonClick(1)"
      >
        重构检测
      </button>
      <button
        class="btn second-nav-button intrusive-button"
        style="width: 13%"
        :class="{ secondActive: buttonIndex === 2 }"
        @click="navButtonClick(2)"
      >
        侵入式修改
      </button>
    </div>
    <div class="second-main">
      <CF
        v-if="buttonIndex === 3"
        v-on:close="onButtonIndex"
        :parentData="localParentData"
      />
      <OS
        v-if="buttonIndex === 0"
        v-on:close="onButtonIndex"
        :parentData="localParentData"
      />
      <RM
        v-if="buttonIndex === 1"
        v-on:close="onButtonIndex"
        :parentData="localParentData"
      />
      <IM
        v-if="buttonIndex === 2"
        v-on:close="onButtonIndex"
        :parentData="localParentData"
      />
      <ME
        v-if="buttonIndex === 4"
        v-on:close="onButtonIndex"
        :parentData="localParentData"
      />
    </div>

  </div>
</template>

<script>
import OS from '@/components/U-DCouplingAntiPattern/OwnershipData.vue'
import IM from '@/components/U-DCouplingAntiPattern/IntrusiveModify.vue'
import RM from '@/components/U-DCouplingAntiPattern/RefactoringMiner.vue'
import CF from '@/components/U-DCouplingAntiPattern/CouplingSurfaces.vue'
import ME from '@/components/U-DCouplingAntiPattern/MetricEvolution.vue'


export default {
  props: {
    parentData: {
      type: Object,
      required: true,
    },
  },
  name: 'DCouplingAntiPattern',
  components: { CF, OS, RM, IM, ME },
  data() {
    return {
      localParentData: { ...this.parentData },
      buttonIndex: 4,
      link: '',
    }
  },
  methods: {
    onButtonIndex(url) {
      this.link = url
      if (this.link.type === 'ener-java') {
        this.$emit('close', { type: 'ener-java' })
      } else if (this.link.type === 'OS') {
        this.navButtonClick(0)
      } else if (this.link.type === 'RM') {
        this.navButtonClick(1)
      } else if (this.link.type === 'IM') {
        this.navButtonClick(2)
      } else if (this.link.type === 'CF') {
        this.navButtonClick(3)
      } else if (this.link.type === 'ME') {
        this.navButtonClick(4)
      }
    },
    navButtonClick(index) {
      this.buttonIndex = index
    },
  },
  watch: {
    parentData(newVal) {
      this.localParentData = { ...newVal }
    },
  },
}
</script>

<style scoped>
.DCouplingAntiPattern {
  height: 100%;
  width: 100%;
  display: inline-block;
}

.second-nav-div {
  display: flex;
  align-items: center; 
  justify-content: center;
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
  background-color: #000000; /* 黑色下划线 */
}

.owner-button {
  margin-left: 250px;
}

.intrusive-button {
  margin-left: auto;
}
</style>
