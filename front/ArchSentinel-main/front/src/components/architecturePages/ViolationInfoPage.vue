<template>
  <div
    class="ViolationInfo"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="right">
        <div class="right-table">
          <el-table
            :data="showList"
            highlight-current-row
            height="30vh"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
            :row-style="{ height: '7vh' }"
            style="border: thin solid white"
          >
            <el-table-column label="ID" prop="ID" width="100"></el-table-column>
            <el-table-column label="rule" prop="rule"></el-table-column>
            <el-table-column
              label="优先级"
              prop="content"
              width="150"
              sortable
            ></el-table-column>
            <el-table-column label="数目" prop="total" sortable></el-table-column>
          </el-table>
        </div>
        <div class="right-chart">
          <div ref="rulePieChart" style="width: 97%; height: 100%"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Papa from 'papaparse'
import * as echarts from 'echarts'
import EventBus from '../eventBus';

export default {
  data() {
    return {
      dataLoading: false,

      fileContent: [],
      showEnerGap: false,
      showList: [
        {
          ID: '1',
          rule: 'classes that are activelyNative should not be extended by classes that are extensive and should not be extended by classes that are private',
          content: 'HIGH',
          total: 0,
        },
        {
          ID: '2',
          rule: 'classes that are public should not be extended by classes that are private',
          content: 'MEDIUM',
          total: 0,
        },
        {
          ID: '3',
          rule: 'classes that have modifier STATIC should be enums',
          content: 'MEDIUM',
          total: 0,
        },
        {
          ID: '4',
          rule: 'classes that have modifier STATIC should not be extended by classes that are public',
          content: 'LOW',
          total: 0,
        },
      ],

      // 实体归属图
      rulePieChart: null,
      ruleTypeCounts: {},
    }
  },
  mounted() {
    this.showEnerGap = true
    this.handleFileRead()
  },
  methods: {
    // 读取文件内容
    async handleFileRead() {
      this.dataLoading = true
      const response = await fetch('/classes.violations.csv') // 从 public 文件夹加载
      const csvContent = await response.text()
      this.fileContent = [] // 初始化数据
      this.ruleTypeCounts = {} // 初始化统计数据

      Papa.parse(csvContent, {
        header: true,
        skipEmptyLines: true,
        step: (row) => {
          //将数据存储
          if (this.fileContent.length < 1000) {
            this.fileContent.push(row.data)
          }
          //存储规则数据
          const rule = row.data.rule || '未知'
          this.ruleTypeCounts[rule] = (this.ruleTypeCounts[rule] || 0) + 1
          const showItem = this.showList.find((item) => item.rule === rule)
          if (showItem) {
            showItem.total = this.ruleTypeCounts[rule]
          }
          EventBus.ViolationInfoList = this.showList
          EventBus.ruleTypeCounts = this.ruleTypeCounts
        },
      })
      await new Promise((resolve) => setTimeout(resolve, 200)) // 延迟200ms
      // 更新实体归属数据
      this.initRulePieChart()
      this.dataLoading = false

      return false
    },

    initRulePieChart() {
      // 初始化边类型饼图
      this.rulePieChart = echarts.init(this.$refs.rulePieChart)
      const pieOption = {
        toolbox: {
          itemSize: 20,
          iconStyle: {
            borderColor: '#000',
          },
          feature: {
            dataView: {},
            restore: {},
            saveAsImage: {},
          },
        },
        title: {
          text: '架构违约检测规则统计',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000',
            fontSize: '1.25rem',
          },
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          textStyle: {
            fontSize: 18,
          },
        },
        legend: {
          orient: 'horizontal',
          top: 'bottom',
          left: 'center',
          textStyle: {
            color: '#000', // 设置文字颜色为黑色
            fontSize: '1rem', // 设置文字大小为1.25rem
          },
        },
        series: [
          {
            name: '架构违约检测规则',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000', // 设置文字颜色为黑色
                fontSize: '1.25rem', // 设置文字大小为1.25rem
              },
            },
            data: Object.keys(this.ruleTypeCounts).map((type) => ({
              value: this.ruleTypeCounts[type],
              name: type,
            })),
          },
        ],
      }
      this.rulePieChart.setOption(pieOption)
    },
    toEner() {
      this.showEnerGap = false
      this.$emit('close', { type: 'ener-java' })
    },
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
  },
}
</script>

<style scoped>
.ViolationInfo {
  height: 120vh;
  width: 100%;
  background-color: #f0f0f0;
  display: inline-block;
}

.main {
  height: 100%;
  width: 100%;
  margin: 1% 0 0 0;
  display: flex;
}

.right {
  width: 100%;
  height: 100%;
  flex-direction: column;
  display: inline-block;
}

.right-table {
  width: 100%;
  height: 30vh;
  margin-bottom: 3vh;
}

.right-chart {
  width: 100%;
  height: 40vh;
  margin-top: 2vh;
}

::v-deep(.el-pagination__total),
::v-deep(.el-pagination__sizes),
::v-deep(.el-pagination__jump) {
  color: #000000;
}

pre {
  background-color: #2d2d2d;
  color: #000000;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
}

h3 {
  text-align: center;
  color: #000000;
}
</style>
