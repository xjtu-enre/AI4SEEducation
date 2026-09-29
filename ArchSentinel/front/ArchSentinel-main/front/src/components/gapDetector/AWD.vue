<template>
  <div
    class="AWD"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main" v-show="fileFind">
      <div class="left">
        <div class="left-half" style="padding-top: 2%">
          <div ref="barChart" style="width: 97%; height: 100%"></div>
        </div>
        <div class="left-half">
          <div ref="pieChart" style="width: 97%; height: 100%"></div>
        </div>
      </div>
      <div class="right">
        <div class="right-table">
          <el-table
            :data="AWDTable"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
          >
            <el-table-column label="父类" prop="superType"></el-table-column>
            <el-table-column label="第三方类" prop="clientClass"></el-table-column>
            <el-table-column label="子类" prop="subType"></el-table-column>
            <el-table-column label="第三方类对父类的依赖数" prop="clientToSuper" width="100" sortable></el-table-column>
            <el-table-column
              label="第三方类对子类的依赖数"
              prop="clientToSub"
              width="100"
              sortable
            ></el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <button class="btn" @click="showAWDGraph(scope.row)">
                  查看详情
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="right-chart">
          <div class="right-chart-awdChart" ref="graphAWDChart"></div>
        </div>
      </div>
    </div>

    <!-- <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 AWD 文件</span> -->
    <!-- </div>  -->
    
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'AWD',
  data() {
    return {
      fileFind: true,
      dataLoading: false,

      jsonData: null,

      // 左侧图
      barChart: null,
      AWDTotal: 0,
      clientToSuperTotal: 0,
      clientToSubTotal: 0,

      pieChart: null,
      linkTotal: 0,
      relationTypeCounts: {},

      relationColors: {}, // 动态生成的边类型颜色映射

      // 右侧
      AWDTable: [],
      graphAWDChart: null,
      selectedAWD: null,
    }
  },
  mounted() {
    this.dataLoading = true
    this.loadDataAndInitChart()
    this.graphAWDChart = echarts.init(this.$refs.graphAWDChart)
  },
  methods: {
    // 加载数据并初始化图表
    loadDataAndInitChart() {
      axios
        .get('/api/results/gap/AWD')
        .then((res) => {
          this.jsonData = res.data.instances
          this.processBarData()
          this.processPieData()
          this.processTableData()
          this.dataLoading = false
          this.initBarChart()
          this.initPieChart()
        })
        .catch((error) => {
          console.error("Error loading data:", error);
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 AWD 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processBarData() {
      // 获取柱状图 的数据 AWD总数 super client sub的总数 第三方依赖父类的总数 第三方依赖子类的总数
      this.AWDTotal = this.jsonData.length

      this.jsonData.forEach((awd) => {
        if (awd.details) {
          if (awd.details.clientClass2superType) {
            this.clientToSuperTotal += awd.details.clientClass2superType.length
          }
          if (awd.details.clientClass2subType) {
            this.clientToSubTotal += awd.details.clientClass2subType.length
          }
        }
      })
    },
    initBarChart() {
      // 初始化节点柱状图
      this.barChart = echarts.init(this.$refs.barChart)
      const barOption = {
        toolbox: {
          itemSize: 20,
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
        title: {
          text: '节点数量统计',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000000',
            fontSize: '1.25rem',
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          textStyle: {
            fontSize: 18,
          },
        },
        xAxis: [
          {
            type: 'category',
            data: ['AWD总数', '第三方类依赖父类总数', '第三方类依赖子类总数'],
            axisLabel: {
              interval: 0, // 保证每个标签都显示
              color: '#000000',
              fontSize: '0.7rem',
            },
          },
        ],
        yAxis: [
          {
            type: 'value',
            axisLabel: {
              interval: 0, // 保证每个标签都显示
              color: '#000000',
              fontSize: '0.7rem',
            },
          },
        ],
        series: [
          {
            name: '节点数',
            type: 'bar',
            data: [
              this.AWDTotal,
              this.clientToSuperTotal,
              this.clientToSubTotal,
            ],
            itemStyle: {
              color: '#5DBB63',
            },
          },
        ],
      }
      this.barChart.setOption(barOption)
    },
    processPieData() {
      // 获取饼状图 关于边 的数据
      this.jsonData.forEach((awd) => {
        if (awd.details) {
          if (awd.details.clientClass2superType) {
            awd.details.clientClass2superType.forEach((rel) => {
              this.relationTypeCounts[rel.relationType] =
                (this.relationTypeCounts[rel.relationType] || 0) + 1
            })
          }
          if (awd.details.clientClass2subType) {
            awd.details.clientClass2subType.forEach((rel) => {
              this.relationTypeCounts[rel.relationType] =
                (this.relationTypeCounts[rel.relationType] || 0) + 1
            })
          }
        }
      })
    },
    initPieChart() {
      // 初始化边类型饼图
      this.pieChart = echarts.init(this.$refs.pieChart)
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
          text: '依赖类型统计',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000000',
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
            color: '#000000',
            fontSize: '1rem',
          },
        },
        series: [
          {
            name: '边类型分布',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000000',
                fontSize: '1.25rem',
              },
            },
            data: Object.keys(this.relationTypeCounts).map((type) => ({
              value: this.relationTypeCounts[type],
              name: type,
            })),
          },
        ],
      }
      this.pieChart.setOption(pieOption)
    },
    processTableData() {
      // 获取表格数据
      this.AWDTable = this.jsonData.map((awd) => ({
        superType: awd.superType.object,
        clientClass: awd.clientClass.object,
        subType: awd.subType.object,
        clientToSuper: awd.details.clientClass2superType.length,
        clientToSub: awd.details.clientClass2subType.length,
        raw: awd, // 保存完整AWD数据，便于后续绘图
      }))

      this.AWDTable = [...this.AWDTable].sort(
        (a, b) =>
          b.clientToSuper + b.clientToSub - (a.clientToSuper + a.clientToSub)
      )
    },
    showAWDGraph(row) {
      this.selectedAWD = row.raw

      this.drawAWDChart()
    },
    drawAWDChart() {
      if (!this.selectedAWD) return

      const { details } = this.selectedAWD

      // 构建节点数据
      const nodes = new Map()
      // nodes.set(superType.object, { id: superType.object, name: superType.object, category: 0 });
      // nodes.set(clientClass.object, {id: clientClass.object, name: clientClass.object, category: 1 })
      // nodes.set(subType.object, { id: subType.object, name: subType.object, category: 2 });
      details.clientClass2superType.forEach((link) => {
        nodes.set(link.fromEntity, {
          id: link.fromEntity,
          name: link.fromEntity,
          category: 1,
          file:
            link.fromEntityFile +
            '(' +
            link.fromEntityLocation.startLine +
            '-' +
            link.fromEntityLocation.endLine +
            ')',
        })
        nodes.set(link.toEntity, {
          id: link.toEntity,
          name: link.toEntity,
          category: 0,
          file:
            link.toEntityFile +
            '(' +
            link.toEntityLocation.startLine +
            '-' +
            link.toEntityLocation.endLine +
            ')',
          depLocation:
            link.fromEntity +
            '(' +
            link.depLocation.startLine +
            '-' +
            link.depLocation.endLine +
            ')',
        })
      })
      details.clientClass2subType.forEach((link) => {
        nodes.set(link.fromEntity, {
          id: link.fromEntity,
          name: link.fromEntity,
          category: 1,
          file:
            link.fromEntityFile +
            '(' +
            link.fromEntityLocation.startLine +
            '-' +
            link.fromEntityLocation.endLine +
            ')',
        })
        nodes.set(link.toEntity, {
          id: link.toEntity,
          name: link.toEntity,
          category: 2,
          file:
            link.toEntityFile +
            '(' +
            link.toEntityLocation.startLine +
            '-' +
            link.toEntityLocation.endLine +
            ')',
          depLocation:
            link.fromEntity +
            '(' +
            link.depLocation.startLine +
            '-' +
            link.depLocation.endLine +
            ')',
        })
      })

      const categoryMap = {
        0: 'superType',
        1: 'clientClass',
        2: 'subType',
      }

      // 转为数组
      const nodeList = Array.from(nodes.values())

      const colors = [
        'gray',
        '#33FF57',
        '#3357FF',
        '#FFC300',
        '#DAF7A6',
        '#FF33C4',
        '#33FFF4',
        '#A633FF',
        '#57FF33',
        '#FF33A6',
        '#33A6FF',
        '#FFA633',
      ]
      let colorIndex = 0

      console.log(this.relationTypeCounts)
      Object.keys(this.relationTypeCounts).forEach((type) => {
        this.relationColors[type] = colors[colorIndex % colors.length]
        colorIndex++
      })

      // 构建边数据
      const links = [
        ...details.clientClass2superType.map((link) => ({
          source: link.fromEntity,
          target: link.toEntity,
          value: link.relationType,
          lineStyle: {
            color: this.relationColors[link.relationType] || '#CCC',
          },
        })),
        ...details.clientClass2subType.map((link) => ({
          source: link.fromEntity,
          target: link.toEntity,
          value: link.relationType,
          lineStyle: {
            color: this.relationColors[link.relationType] || '#CCC',
          },
        })),
      ]

      // 绘图选项
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
          formatter: (param) => {
            if (param.dataType === 'node') {
              if (param.data.category === 1) {
                return `
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${
                          param.data.name
                        }</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${
                          categoryMap[param.data.category]
                        }</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${
                          param.data.file
                        }</span>
                        `
              } else {
                return `
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${
                          param.data.name
                        }</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${
                          categoryMap[param.data.category]
                        }</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${
                          param.data.file
                        }</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">第三方类调用位置：${
                          param.data.depLocation
                        }</span>
                        `
              }
            }
            if (param.dataType === 'edge') {
              return `
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">关系类型：${param.data.value}</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">起始节点：${param.data.source}</span>
                        <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">目标节点：${param.data.target}</span>
                    `
            }
          },
        },
        toolbox: {
          itemSize: 20,
          iconStyle: {
            borderColor: 'black',
          },
          feature: {
            dataZoom: {},
            dataView: {},
            restore: {},
            saveAsImage: {},
          },
        },
        legend: {
          data: ['superType', 'clientClass', 'subType'],
          bottom: 10,
          textStyle: {
            fontSize: 15,
            color: 'black',
          },
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 300, // 设置节点之间的排斥力，值越大，节点越远
              gravity: 0.05, // 调整引力，值越大，节点越靠近
              edgeLength: [50, 300], // 控制连接边的长度
            },
            roam: true, // 支持鼠标缩放和平移
            symbolSize: 40, // 节点大小
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
            edgeLabel: {
              show: true,
              formatter: (params) => params.data.value,
              textStyle: {
                fontSize: 15,
                color: 'black',
              },
            },
            data: nodeList,
            links: links,
            categories: [
              { name: 'superType' },
              { name: 'clientClass' },
              { name: 'subType' },
            ],
          },
        ],
      }

      this.graphAWDChart.setOption(option)
    },
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.graphAWDChart) {
      this.graphAWDChart.dispose()
    }
    if (this.barChart) {
      this.barChart.dispose()
    }
    if (this.pieChart) {
      this.pieChart.dispose()
    }
  },
}
</script>

<style scoped>
.AWD {
  height: 100%;
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

.left {
  height: 96%;
  width: 20%;
  flex-direction: column;
  margin: 0 1% 0 0.5%;
  display: inline-block;
  border: thin solid white;
}

.right {
  height: 96%;
  width: 78%;
  display: flex;
  flex-direction: column;
}

.left-half {
  height: 48%;
  width: 100%;
  display: flex;
}

.right-table {
  height: 44%;
  width: 98%;
  margin-bottom: 1%;
  display: flex;
}

.right-chart {
  width: 100%;
  height: 55%;
  display: flex;
}

.right-chart-awdChart {
  height: 100%;
  width: 100%;
}

.el-table {
  width: 100%;
  height: 100%;
  background-color: transparent;
}

.el-table::before {
  height: 0;
}

.el-table th,
.el-table td {
  border: none !important;
  background-color: transparent !important;
}
</style>
