<template>
  <div
    class="Ownership"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <!-- <div class="right"> -->
        <div class="right-table">
          <el-table
            :data="OSTable"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
            style="border: thin solid white;"
          >
            <el-table-column label="ID" prop="ID" width="100"></el-table-column>
            <el-table-column
              label="ownership"
              prop="ownership"
            ></el-table-column>
            <el-table-column
              label="category"
              prop="category"
              width="150"
            ></el-table-column>
            <el-table-column
              label="qualifiedName"
              prop="qualifiedName"
            ></el-table-column>
          </el-table>
        </div>
        <div class="right-chart">
          <div
            ref="ownershipPieChart"
            class="chart-container"
            style="width: 100%; height: 100%"
          ></div>
          <div
            ref="categoryPieChart"
            class="chart-container"
            style="width: 100%; height: 100%"
          ></div>
        </div>
      <!-- </div> -->
    </div>
  </div>
</template>

<script>
import Papa from 'papaparse'
import * as echarts from 'echarts'
import EventBus from '../eventBus'

export default {
  data() {
    return {
      dataLoading: false,

      fileContent: [],
      showEnerGap: false,
      showList: [],
      fileList: [],

      //表格数据
      OSTable: [],

      // 实体归属图
      ownershipPieChart: null,
      ownershipTypeCounts: {},

      //实体类别图
      categoryPieChart: null,
      categoryTypeCounts: {},
    }
  },
  mounted() {
    (this.showEnerGap = true), this.handleFileRead()
  },
  methods: {
    // 读取文件内容
    async handleFileRead() {
      this.dataLoading = true
      const response = await fetch('/final_ownership.csv') // 从 public 文件夹加载
      const csvContent = await response.text()
      this.fileContent = [] // 初始化数据
      this.ownershipTypeCounts = {} // 初始化统计数据
      this.categoryTypeCounts = {} // 初始化统计数据

      Papa.parse(csvContent, {
        header: true,
        skipEmptyLines: true,
        step: (row) => {
          //将数据存储
          if (this.fileContent.length < 1000) {
            this.fileContent.push(row.data)
          }
          //存储实体归属数据
          const ownership = row.data.ownership || '未知'
          this.ownershipTypeCounts[ownership] =
            (this.ownershipTypeCounts[ownership] || 0) + 1
          //存储实体类别数据
          const category = row.data.category || '未知'
          this.categoryTypeCounts[category] =
            (this.categoryTypeCounts[category] || 0) + 1
        },
      })

      await new Promise((resolve) => setTimeout(resolve, 200)) // 延迟200ms
      EventBus.categoryTypeCounts = this.categoryTypeCounts
      EventBus.ownershipTypeCounts = this.ownershipTypeCounts

      // 更新表格数据
      this.processTableData()
      // 更新实体归属数据
      this.initOnwershipPieChart()
      // 更新实体类别数据
      this.initCategoryPieChart()
      this.dataLoading = false

      return false
    },
    processTableData() {
      this.OSTable = this.fileContent.map((row, index) => ({
        ID: parseInt(row.id || index, 10) + 1,
        ownership: row.ownership || '-',
        category: row.category || '-',
        qualifiedName: row.qualifiedName || '-',
      }))

      EventBus.OSList = this.OSTable
    },
    //实体归属数据加载
    // processOwnershipPieData() {
    //   this.ownershipTypeCounts = {}
    //   this.fileContent.forEach((row) => {
    //     this.ownershipTypeCounts[row.ownership] =
    //       (this.ownershipTypeCounts[row.ownership] || 0) + 1
    //   })
    // },
    initOnwershipPieChart() {
      // 初始化边类型饼图
      this.ownershipPieChart = echarts.init(this.$refs.ownershipPieChart)
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
          text: '实体归属统计',
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
            name: '实体归属',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000', // 设置文字颜色为黑色
                fontSize: '1.25rem', // 设置文字大小为1.25rem
              },
            },
            data: Object.keys(this.ownershipTypeCounts).map((type) => ({
              value: this.ownershipTypeCounts[type],
              name: type,
            })),
          },
        ],
      }
      this.ownershipPieChart.setOption(pieOption)
    },
    //实体类别数据加载
    // processCategoryPieData() {
    //   this.categoryTypeCounts = {}
    //   this.fileContent.forEach((row) => {
    //     this.categoryTypeCounts[row.category] =
    //       (this.categoryTypeCounts[row.category] || 0) + 1
    //   })
    // },
    initCategoryPieChart() {
      // 初始化边类型饼图
      this.categoryPieChart = echarts.init(this.$refs.categoryPieChart)
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
          text: '实体类别统计',
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
            name: '实体类别',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000', // 设置文字颜色为黑色
                fontSize: '1.25rem', // 设置文字大小为1.25rem
              },
            },
            data: Object.keys(this.categoryTypeCounts).map((type) => ({
              value: this.categoryTypeCounts[type],
              name: type,
            })),
          },
        ],
      }
      this.categoryPieChart.setOption(pieOption)
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
.Ownership {
  height: 100%;
  width: 100%;
  display: inline-block;
  background-color: #f0f0f0;
  border: none;
}

.main {
  height: 100%;
  width: 100%;
  margin: 1% 0 0 0;
  display: flex;
  flex-direction: column;
}

/* .right {

} */

.right-table {
  height: 35%;
  width: 94%;
  margin-left: 3%;
  display: flex;
}

.right-chart {
  margin-top: 1%;
  width: 100%;
  height: 60%;
  display: flex;
  /* justify-content: space-around; */
  /* align-items: center; */
}

.chart-container {
  width: 45%;
  height: 100%;
  margin: 0;
  background-color: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10%;
}

:deep(.el-table) {
  background-color: transparent;
  border: none;
}

:deep(.el-table td),
:deep(.el-table th) {
  background-color: transparent;
  border: none;
  color: #000;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>
