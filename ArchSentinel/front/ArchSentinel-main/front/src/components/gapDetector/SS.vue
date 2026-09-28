<template>
  <div class="SS" v-loading.fullscreen.lock="dataLoading" element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.3)">
    <div class="main">
      <div class="main-table">
        <el-table :data="SSTable" highlight-current-row height="100%" :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle">
          <el-table-column label="目标名" prop="object" />
          <el-table-column label="CC" prop="CC" width="200" sortable/>
          <el-table-column label="CM" prop="CM" width="200" sortable/>
          <el-table-column label="FANOUT" prop="FANOUT" width="200" sortable/>
          <el-table-column label="操作" width="250">
            <template slot-scope="scope">
              <button @click="viewDetails(scope.row)" type="text" size="small" class="btn">查看详情</button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="main-chart">
        <div ref="chartCC" style="height: 100%; width: 12%; margin-left: 2%"></div>
        <div ref="chartCM" style="height: 100%; width: 12%"></div>
        <div ref="chartFANOUT" style="height: 100%; width: 12%; margin-right: 2%"></div>
        <div ref="detailChart" style="height: 100%; width: 60%"></div>
      </div>
    </div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 SS 文件</span>
    </div>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'SS',
  data() {
    return {
      fileFind: true,

      jsonData: null,
      dataLoading: false,

      SSTable: [],

      chartCC: null,
      chartCM: null,
      chartFANOUT: null,
      detailChart: null,
    }
  },
  mounted() {
    this.dataLoading = true;
    this.loadDataAndInitChart();
    this.chartCC = echarts.init(this.$refs.chartCC);
    this.chartCM = echarts.init(this.$refs.chartCM);
    this.chartFANOUT = echarts.init(this.$refs.chartFANOUT);
    this.detailChart = echarts.init(this.$refs.detailChart);
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get('/DownFiles-SS.json')
        .then((res) => {
          this.jsonData = res.data.shotgunSurgeryStructureList
          this.processTableData()
          this.processBoxplotChartData()
          this.dataLoading = false
          this.initBoxplotChart()
        })
        .catch((error) => {
          console.error('Error loading data:', error)
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 SS 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processTableData() {
      this.SSTable = this.jsonData.map((item) => ({
        id: item.id,
        object: item.object,
        CC: item.CC,
        CM: item.CM,
        FANOUT: item.FANOUT,
        raw: item,
      }))
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
      const rawCC = this.SSTable.map((item) => item.CC)
      const rawCM = this.SSTable.map((item) => item.CM)
      const rawFANOUT = this.SSTable.map((item) => item.FANOUT)

      const boxDataCC = this.calculateBoxplotData(rawCC)
      const boxDataCM = this.calculateBoxplotData(rawCM)
      const boxDataFANOUT = this.calculateBoxplotData(rawFANOUT)

      this.initBoxplotChart(this.chartCC, 'CC', rawCC, boxDataCC)
      this.initBoxplotChart(this.chartCM, 'CM', rawCM, boxDataCM)
      this.initBoxplotChart(
        this.chartFANOUT,
        'FANOUT',
        rawFANOUT,
        boxDataFANOUT
      )
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
    viewDetails(row) {
      const rawData = row.raw;
      
      const nodes = [];
      const links = [];

      // 添加本方法节点
      nodes.push({
        id: `method-${rawData.object}`,
        name: rawData.object,
        category: 0,
      });

      // 处理 callClassSet (调用的类 - FANOUT)
      rawData.callClassSet.forEach((item, index) => {
        nodes.push({
          id: `fanout-${index}-${item.object}`,
          name: item.object,
          file: item.file,
          category: 1,
        });
        links.push({
          source: `method-${rawData.object}`,
          target: `fanout-${index}-${item.object}`,
        });
      });

      // 处理 callByClassSet (调用本方法的类 - CC)
      rawData.callByClassSet.forEach((item, index) => {
          nodes.push({
              id: `cc-${index}-${item.object}`,
              name: item.object,
              file: item.file,
              category: 2, 
          });
          links.push({
              source: `cc-${index}-${item.object}`,
              target: `method-${rawData.object}`,
          });
      });

      // 处理 callBySet (调用本方法的方法 - CM)
      rawData.callBySet.forEach((item, index) => {
        nodes.push({
          id: `cm-${index}-${item.object}`,
          name: item.object,
          file: item.file,
          modifier: item.modifier,
          category: 3, 
        });
        links.push({
          source: `cm-${index}-${item.object}`,
          target: `method-${rawData.object}`,
        });
      });

      const category = [
        { name: '本方法'},
        { name: 'callClassSet'},
        { name: 'callByClassSet'},
        { name: 'callBySet'},
      ];
      const categories = ['本方法', 'callClassSet 本方法调用的类', 'callByClassSet 类调用的本方法', 'callBySet 方法调用的该方法']

      const option = {
        title: { 
          text: '调用关系可视化', 
          left: 'center', 
          top: 'top',
          textStyle: {
            color: '#000000',
            fontSize: '1.25rem',
          },
        },
        tooltip: {
          formatter: (params) => {
            if (params.dataType === 'node') {
              if (params.data.category === 3) {
                return `
                  <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">节点名称：${params.data.name}</span><br>
                  <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">类型：${categories[params.data.category]}</span><br>
                  <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">修饰符：${params.data.modifier}</span><br>                              
                  <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">位置：${params.data.file}</span><br>  
                `;
              }
              return `
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">节点名称：${params.data.name}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">类型：${categories[params.data.category]}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">位置：${params.data.file}</span><br>                              
              `;
            } else {
              return `
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">起始节点：${params.data.source}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">目标节点：${params.data.target}</span><br>
              `;
            }
          },
        },
        toolbox: {
          itemSize: 20,
          iconStyle: {
            borderColor: '#000000',
          },
          feature: {
            dataZoom: {},
            dataView: {},
            restore: {},
            saveAsImage: {},
          },
        },
        legend: {
          data: category.map((cat) => cat.name),
          bottom: 10,
          textStyle: {
            fontSize: 15,
            color: '#000000',
          },
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 250, // 设置节点之间的排斥力，值越大，节点越远
              gravity: 0.05, // 调整引力，值越大，节点越靠近
              edgeLength: [50, 300], // 控制连接边的长度
            },
            symbolSize: 40, // 节点大小
            roam: true, // 支持鼠标缩放和平移
            label: {
              show: false,
            },
            edgeSymbol: ['none', 'arrow'], // 连接线的两端使用的符号
            edgeSymbolSize: [4, 10], // 连接线符号的大小
            lineStyle: {
              // 连接线的样式配置
              width: 1,
              curveness: 0.1,
            },
            emphasis: {
              // 节点高亮时的配置
              focus: 'adjacency', // 当节点被高亮时，与其相邻的节点和连接线也会被高亮
              label: {
                show: false, // 鼠标悬停时显示标签
                // formatter: "{b}", // 标签的格式化函数
              },
            },
            data: nodes,
            links: links,
            categories: category,
          },
        ],
      };
      this.detailChart.setOption(option);
    },
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
  },
  beforeDestroy() {
    if (this.chartCC) {
      this.chartCC.dispose()
    }
    if (this.chartCM) {
      this.chartCM.dispose()
    }
    if (this.chartFANOUT) {
      this.chartFANOUT.dispose()
    }
    if (this.detailChart) {
      this.detailChart.dispose()
    }
  },
}
</script>

<style scoped>
.SS {
  height: 100%;
  width: 100%;
  display: inline-block;
  background-color: #f0f0f0;
  /* 修改背景色 */
  border: none;
  /* 移除边框 */
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

/* 添加表格样式 */
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
