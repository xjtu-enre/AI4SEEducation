<template>
  <div
    class="MH"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="main-table">
        <el-table
          :data="MHTable"
          highlight-current-row
          height="100%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column label="起点类" prop="start" />
          <el-table-column label="终点类" prop="end" />
          <el-table-column label="多重路径数" prop="pathCount" width="200" sortable/>
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
        <!-- 展示 MH 的分布 -->
        <div ref="barChart" class="main-chart-left"></div>
        <!-- 展示 MH中 的分布 查看详情按钮 -->
        <div ref="detailChart" class="main-chart-right"></div>
      </div>
    </div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 MH 文件</span>
    </div>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'MH',
  data() {
    return {
      fileFind: true,

      jsonData: null,
      dataLoading: false,

      MHTable: [],
      barChart: null,
      detailChart: null,

      relationTypeCounts: [],
      relationColors: {},
    }
  },
  mounted() {
    this.dataLoading = true
    this.loadDataAndInitChart()
    this.barChart = echarts.init(this.$refs.barChart)
    this.detailChart = echarts.init(this.$refs.detailChart)
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get('/api/results/gap/MH')
        .then((res) => {
          this.jsonData = res.data.instances
          this.processTableData()
          this.processBarData()
          this.dataLoading = false
          this.initBar()
        })
        .catch((error) => {
          console.error('Error loading data:', error)
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 MH 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processTableData() {
      this.MHTable = this.jsonData.map((instance) => ({
        start: instance.start.object,
        end: instance.end.object,
        pathCount: instance.multipath.length, // 多重路径数
        raw: instance,
      }))

      this.relationTypeCounts = ['MH', 'Implement', 'inherit']

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

      this.relationTypeCounts.forEach((type) => {
        this.relationColors[type] = colors[colorIndex % colors.length]
        colorIndex++
      })
    },
    processBarData() {
      const lengthDistribution = {}

      this.jsonData.forEach((item) => {
        if (item.multipath && Array.isArray(item.multipath)) {
          item.multipath.forEach((path) => {
            const length = path.length
            lengthDistribution[length] = (lengthDistribution[length] || 0) + 1
          })
        }
      })

      // 将分布数据转换为 ECharts 数据格式
      return {
        category: Object.keys(lengthDistribution).map(Number), // 转换为数字类型
        data: Object.values(lengthDistribution),
      }
    },
    initBar() {
      const { category, data } = this.processBarData()

      const option = {
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
          text: '路径长度分布',
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
            color: '#000000',
          },
        },
        xAxis: {
          type: 'category',
          data: category,
          name: '路径长度',
          axisLabel: {
            interval: 0, // 保证每个标签都显示
            color: '#000000',
            fontSize: '1rem',
          },
          nameTextStyle: {
            color: '#000000',
            fontSize: 18,
          },
        },
        yAxis: {
          type: 'value',
          name: '路径数量',
          axisLabel: {
            interval: 0, // 保证每个标签都显示
            color: '#000000',
            fontSize: '1rem',
          },
          nameTextStyle: {
            color: '#000000',
            fontSize: 18,
          },
        },
        series: [
          {
            type: 'bar',
            data: data,
            barWidth: '50%',
            itemStyle: {
              color: '#5DBB63',
            },
          },
        ],
      }

      // 设置图表
      this.barChart.setOption(option)
    },
    processDetailsData(details) {
      const links = []
      const nodeMap = new Map()

      // 颜色生成器，生成唯一颜色
      const colorGenerator = (index) => {
        const colors = [
          '#5470C6',
          '#91CC75',
          '#FAC858',
          '#EE6666',
          '#73C0DE',
          '#3BA272',
          '#FC8452',
          '#9A60B4',
          '#EA7CCC',
        ]
        return colors[index % colors.length] // 循环使用颜色数组
      }

      // 遍历 multipath，添加节点和连线
      details.multipath.forEach((path, pathIndex) => {
        const pathColor = colorGenerator(pathIndex)

        path.forEach((subPath) => {
          // 添加节点
          const fromCategory =
            subPath.fromEntity === details.start.object ? 'start' : 'middle'

          const fromNode = {
            id: subPath.fromEntity,
            name: subPath.fromEntity,
            modifier: subPath.fromEntityModifier,
            type: subPath.fromEntityType,
            file: subPath.fromEntityFile,
            location: subPath.fromEntityLocation,
            category: fromCategory,
          }

          // 添加 to 节点
          const toCategory =
            subPath.toEntity === details.end.object ? 'end' : 'middle'
          const toNode = {
            id: subPath.toEntity,
            name: subPath.toEntity,
            modifier: subPath.toEntityModifier,
            type: subPath.toEntityType,
            file: subPath.toEntityFile,
            location: subPath.toEntityLocation,
            category: toCategory,
          }

          if (!nodeMap.has(fromNode.id)) nodeMap.set(fromNode.id, fromNode)
          if (!nodeMap.has(toNode.id)) nodeMap.set(toNode.id, toNode)

          // 添加边
          links.push({
            source: fromNode.id,
            target: toNode.id,
            value: subPath.relationType,
            color: pathColor,
          })
        })
      })
      return {
        nodes: Array.from(nodeMap.values()),
        links,
      }
    },
    viewDetails(row) {
      // 数据解析
      const { nodes, links } = this.processDetailsData(row.raw)

      // 图表配置
      const option = {
        title: {
          text: '路径可视化',
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
              return `
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">节点名称：${param.data.name}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">类型：${param.data.type}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">路径类型：${param.data.category}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">修饰符：${param.data.modifier}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">位置：${param.data.file}\n(${param.data.location.startLine}-${param.data.location.endLine})</span>
              `
            }
            if (param.dataType === 'edge') {
              return `
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">关系类型：${param.data.value}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">起始节点：${param.data.source}</span><br>
                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;color:#000000;">目标节点：${param.data.target}</span>
              `
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
          data: ['start', 'middle', 'end'],
          bottom: 20,
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
            edgeLabel: {
              show: true,
              formatter: (params) => params.data.value,
              textStyle: {
                fontSize: 12,
                color: '#000000',
              },
            },
            data: nodes,
            links: links.map((link) => ({
              ...link,
              lineStyle: {
                color: link.color,
                width: 2,
              },
            })),
            categories: [
              { name: 'start' },
              { name: 'middle' },
              { name: 'end' },
            ],
          },
        ],
      }

      this.detailChart.setOption(option)
    },
    getHeaderStyle() {
      return {
        backgroundColor: 'transparent',
        color: '#000',
        textAlign: 'center',
        fontWeight: 'bold',
        border: 'none',
      }
    },
    getCellStyle() {
      return {
        backgroundColor: 'transparent',
        color: '#000',
        textAlign: 'center',
        border: 'none',
      }
    },
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.barChart) {
      this.barChart.dispose()
    }
    if (this.detailChart) {
      this.detailChart.dispose()
    }
  },
}
</script>

<style scoped>
.MH {
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
  height: 98%;
  width: 40%;
  display: flex;
}

.main-chart-right {
  height: 100%;
  width: 60%;
  display: flex;
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
