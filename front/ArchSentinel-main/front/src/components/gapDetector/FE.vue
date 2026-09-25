<template>
  <div
    class="FE"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="main-table">
        <el-table
          :data="FETable"
          highlight-current-row
          height="100%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column label="目标名" prop="object" />
          <el-table-column label="ATFD" prop="ATFD" width="150" sortable/>
          <el-table-column label="LAA" prop="LAA" width="250" sortable/>
          <el-table-column label="FDP" prop="FDP" width="150" sortable/>
          <el-table-column label="操作" width="250">
            <template slot-scope="scope">
              <button
                @click="viewDetails(scope.row)"
                type="text"
                size="small"
                class="btn"
                >查看详情</button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="main-chart">
        <!-- 展示 FE 的分布 -->
        <div
          ref="barChart"
          style="height: 100%; width: 60%; margin-right: 1%"
        ></div>
        <div ref="chartATFD" style="height: 100%; width: 13%;margin-right: 0.5%;"></div>
        <div ref="chartFDP" style="height: 100%; width: 13%;margin-right: 0.5%;"></div>
        <div ref="chartLAA" style="height: 100%; width: 13%"></div>
      </div>
    </div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 FE 文件</span>
    </div>

    <!-- 模态框显示详情 -->
    <el-dialog
      :visible.sync="showDetails"
      title="节点详情"
      width="82%"
      :before-close="closeDetails"
      custom-class="custom-dialog"
    >
      <div
        v-if="selectedDetails"
        style="padding-bottom: 2%; background-color: white"
      >
        <p style="font-size: 1.25rem;color: #000000;padding-top: 1%;">
          <strong style="font-size: 1.25rem; color: #000000;">目标名：</strong>
          {{ selectedDetails.object }}
        </p>
        <p>
          <strong style="font-size: 1.25rem; color: #000000"
            >外部属性引用详情表</strong
          >
        </p>
        <div style="height: 800px; background-color: #f0f0f0">
          <el-table
            :data="selectedDetails.raw.funcForeignAttributesSet"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
          >
            <el-table-column
              prop="object"
              label="属性名"
              width="450"
            ></el-table-column>
            <el-table-column
              prop="modifier"
              label="修饰符"
              width="450"
            ></el-table-column>
            <el-table-column label="位置">
              <template #default="scope">
                <div style="color: #000000">
                  文件位置: {{ scope.row.file }} ({{
                    scope.row.location.startLine
                  }}
                  - {{ scope.row.location.endLine }}),
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeDetails" style="color: #000000">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'FE',
  data() {
    return {
      fileFind: true,

      jsonData: null,
      dataLoading: false,

      FETable: [],

      barChart: null,
      chartATFD: null,
      chartFDP: null,
      chartLAA: null,

      showDetails: false, // 控制模态框显示
      selectedDetails: null, // 当前选中的节点详情
    }
  },
  mounted() {
    this.dataLoading = true
    this.loadDataAndInitChart()
    this.barChart = echarts.init(this.$refs.barChart)
    this.chartATFD = echarts.init(this.$refs.chartATFD)
    this.chartFDP = echarts.init(this.$refs.chartFDP)
    this.chartLAA = echarts.init(this.$refs.chartLAA)
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get('/DownFiles-FE.json')
        .then((res) => {
          this.jsonData = res.data.instances
          this.processTableData()
          this.processBoxplotChartData()
          this.dataLoading = false
          this.initBarChart()
          this.initBoxplotChart()
        })
        .catch((error) => {
          console.error('Error loading data:', error)
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 FE 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processTableData() {
      this.FETable = this.jsonData
        .map((item) => ({
          id: item.id,
          object: item.object,
          ATFD: item.ATFD,
          LAA: item.LAA,
          FDP: item.FDP,
          raw: item,
        }))
        .sort((a, b) => b.LAA - a.LAA)
    },
    initBarChart() {
      const option = {
        title: {
          text: '每个FE的统计指标',
          left: 'center',
          textStyle: {
            fontSize: '1rem',
            color: '#000000',
          },
        },
        toolbox: {
          itemSize: 20,
          top: -5,
          right: 200,
          iconStyle: {
            borderColor: '#000',
          },
          feature: {
            dataView: {},
            magicType: {
              show: true,
              type: ['line', 'bar'],
              title: {
                line: '折线图',
                bar: '柱状图',
              },
            },
            restore: {},
            saveAsImage: {},
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
          formatter: (params) => {
            // 使用HTML表格对齐内容
            let tooltipContent = `<div><strong>${params[0].axisValue}</strong></div>`
            tooltipContent += `<table style="width: 100%;">`
            params.forEach((item) => {
              tooltipContent += `
                      <tr>
                          <td style="text-align: left;">${item.seriesName}</td>
                          <td style="text-align: right;">${item.value}</td>
                      </tr>
                  `
            })
            tooltipContent += `</table>`
            return tooltipContent
          },
          textStyle: {
            color: 'gray',
            fontSize: 18,
          },
          backgroundColor: '#FFFFFF', // Tooltip背景色
          extraCssText: 'box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);', // 增强视觉效果
        },
        legend: {
          data: ['ATFD', 'LAA', 'FDP'],
          bottom: 15,
          textStyle: {
            fontSize: '1.25rem',
            color: '#000000',
          },
        },
        xAxis: {
          type: 'category',
          data: this.FETable.map((item) => item.object),
          axisLabel: {
            show: false, // 隐藏 X 轴的标签
            // interval: 0,
            // rotate: 30, // 旋转显示，防止重叠
            // fontSize: 12, // 调整字体大小
            // color: '#000000',
          },
        },
        yAxis: [
          {
            type: 'value',
            name: 'ATFD / FDP',
            position: 'right',
            axisLabel: {
              formatter: '{value}',
              color: '#000000', // X轴标签颜色
              fontSize: 15, // 调整字体大小
            },
            nameTextStyle: {
              color: '#000000',
              fontSize: 18,
            },
          },
          {
            type: 'value',
            name: 'LAA',
            position: 'left',
            axisLabel: {
              formatter: '{value}',
              color: '#000000', // X轴标签颜色
              fontSize: 15, // 调整字体大小
            },
            nameTextStyle: {
              color: '#000000',
              fontSize: 18,
            },
          },
        ],
        series: [
          {
            name: 'ATFD',
            type: 'bar',
            data: this.FETable.map((item) => item.ATFD),
            yAxisIndex: 0,
            itemStyle: {
              color: '#5470c6',
            },
          },
          {
            name: 'LAA',
            type: 'bar',
            data: this.FETable.map((item) => item.LAA),
            yAxisIndex: 1,
            itemStyle: {
              color: '#fac858',
            },
          },
          {
            name: 'FDP',
            type: 'bar',
            data: this.FETable.map((item) => item.FDP),
            yAxisIndex: 0,
            itemStyle: {
              color: '#91cc75',
            },
          },
        ],
      }

      this.barChart.setOption(option)
    },
    calculateBoxplotData(data) {
      data.sort((a, b) => a - b)
      const min = Math.min(...data)
      const max = Math.max(...data)
      const q1 = this.calculatePercentile(data, 25)
      const q2 = this.calculatePercentile(data, 50) // Median
      const q3 = this.calculatePercentile(data, 75)
      return [min, q1, q2, q3, max]
    },
    calculatePercentile(data, percentile) {
      const index = (percentile / 100) * (data.length - 1)
      const lower = Math.floor(index)
      const upper = lower + 1
      const weight = index - lower

      if (upper >= data.length) {
        return data[lower]
      }
      return data[lower] * (1 - weight) + data[upper] * weight
    },
    processBoxplotChartData() {
      // 准备数据
      const rawATFD = this.FETable.map((item) => item.ATFD)
      const rawFDP = this.FETable.map((item) => item.FDP)
      const rawLAA = this.FETable.map((item) => item.LAA)

      const boxDataATFD = this.calculateBoxplotData(rawATFD)
      const boxDataFDP = this.calculateBoxplotData(rawFDP)
      const boxDataLAA = this.calculateBoxplotData(rawLAA)

      this.initBoxplotChart(this.chartATFD, 'ATFD', rawATFD, boxDataATFD)
      this.initBoxplotChart(this.chartFDP, 'FDP', rawFDP, boxDataFDP)
      this.initBoxplotChart(this.chartLAA, 'LAA', rawLAA, boxDataLAA)
    },
    initBoxplotChart(chart, title, rawData, boxData) {
      const option = {
        grid: {
          bottom: "13%",
        },
        title: {
          text: `${title} Boxplot`,
          textStyle: { color: '#000000' },
          left: 'center',
        },
        tooltip: {
          trigger: "item",
          formatter: (param) => {
            if (param.componentType === "series") {
              if (param.seriesType === "boxplot") {
                return `
                  ${param.seriesName}<br>
                  Min: ${boxData[0]}<br>
                  Q1: ${boxData[1]}<br>
                  Median: ${boxData[2]}<br>
                  Q3: ${boxData[3]}<br>
                  Max: ${boxData[4]}
                `;
              } else if (param.seriesType === "scatter") {
                const dataIndex = param.dataIndex;
                const object = rawData[dataIndex].object;
                const value = param.value[1];
                return `
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${object}</span><br>
                  <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">${param.seriesName}: ${value}</span>
                `;
              }
            }
            return "";
          },
        },
        graphic: {
          type: "group",
          left: "center",
          bottom: 13, // 靠近图表底部
          children: [
            // 第一行文本
            {
              type: "text",
              left: -200,
              top: 0,
              style: {
                text: `Min: ${this.formatNumber(boxData[0])}`, // 使用格式化函数
                fill: "black",
                font: "16px Arial",
              },
            },
            {
              type: "text",
              left: -100,
              top: 0,
              style: {
                text: `Q1: ${this.formatNumber(boxData[1])}`, // 使用格式化函数
                fill: "black",
                font: "16px Arial",
              },
            },
            {
              type: "text",
              left: 0,
              top: 0,
              style: {
                text: `Median: ${this.formatNumber(boxData[2])}`, // 使用格式化函数
                fill: "black",
                font: "16px Arial",
              },
            },
            // 第二行文本
            {
              type: "text",
              left: -200,
              top: 20, // 第二行文本的高度位置
              style: {
                text: `Q3: ${this.formatNumber(boxData[3])}`, // 使用格式化函数
                fill: "black",
                font: "16px Arial",
              },
            },
            {
              type: "text",
              left: -100,
              top: 20,
              style: {
                text: `Max: ${this.formatNumber(boxData[4])}`, // 使用格式化函数
                fill: "black",
                font: "16px Arial",
              },
            },
          ],
        },
        xAxis: {
          type: 'category',
          data: [title],
          axisLabel: { color: '#000000' },
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#000000' },
        },
        series: [
          {
            name: title,
            type: 'boxplot',
            data: [[0, ...boxData]],
            // itemStyle: { color: "#5470c6" },
            itemStyle: {
              color: 'red', // 填充颜色为白色
              borderColor: 'red', // 边框颜色为白色
            },
          },
          {
            name: `${title} Points`,
            type: 'scatter',
            data: rawData.map((value) => [0, value]),
            itemStyle: { color: 'green' },
          },
        ],
      }
      chart.setOption(option)
    },
    formatNumber(num) {
      // 检查数字是否有小数部分且小数部分的长度超过5
      if (num % 1 !== 0 && num.toString().split('.')[1].length > 5) {
        return num.toFixed(5); // 保留五位小数
      }
      return num; // 小数部分不超过5位，直接返回原数字
    },
    // 查看详情
    viewDetails(row) {
      this.selectedDetails = row // 保存当前行数据
      this.showDetails = true // 打开模态框
    },
    // 关闭详情
    closeDetails() {
      this.showDetails = false
      this.selectedDetails = null
    },
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0; border: none;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0; border: none;'
    },
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.barChart) {
      this.barChart.dispose()
    }
    if (this.chartATFD) {
      this.chartATFD.dispose()
    }
    if (this.chartATFD) {
      this.chartATFD.dispose()
    }
    if (this.chartATFD) {
      this.chartATFD.dispose()
    }
  },
}
</script>

<style scoped>
.FE {
  height: 100%;
  width: 100%;
  display: inline-block;
  background-color: #f0f0f0; /* 修改背景色 */
  border: none; /* 移除边框 */
}

.main {
  height: 100%;
  width: 100%;
  margin: 1% 0 0 0;
  display: flex;
  flex-direction: column;
}

.main-table {
  height: 30%;
  width: 97%;
  margin-left: 1.5%;
  margin-bottom: 1%;
}

.main-chart {
  height: 64%;
  width: 100%;
  display: flex;
}

.main-chart-left {
  height: 100%;
  width: 60%;
  display: flex;
}

.main-chart-right {
  height: 100%;
  width: 40%;
  display: flex;
}

/* 添加表格样式 */
:deep(.el-table) {
  background-color: transparent;
  border: none;
}

:deep(.el-table td),
:deep(.el-table th) {
  background-color: transparent;
  border: none;
  color: #000000;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(0, 0, 0, 0.1);
}

/* 添加弹窗样式 */
:deep(.custom-dialog) {
  background-color: #f0f0f0;
}

:deep(.custom-dialog .el-dialog__title) {
  color: #000000;
}

:deep(.custom-dialog .el-dialog__body) {
  color: #000000;
}

:deep(.custom-dialog .el-dialog__header) {
  color: #000000;
}
</style>
