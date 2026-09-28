<template>
  <div
    class="CH"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="main-table">
        <el-table
          :data="CHTable"
          highlight-current-row
          height="100%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column label="父类" prop="superType" />
          <el-table-column label="子类" prop="subType" />
          <el-table-column label="依赖总数" width="150" prop="relationCount" sortable/>
          <el-table-column label="操作" width="150">
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
        <!-- 展示 类节点 的分布 -->
        <div ref="graphChart" class="main-chart-left"></div>
        <!-- 展示 方法节点 的分布 查看详情按钮 -->
        <div ref="detailChart" class="main-chart-right"></div>
      </div>
    </div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 CH 文件</span>
    </div>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'CH',
  data() {
    return {
      fileFind: true,

      jsonData: null,
      dataLoading: false,

      graphData: null,
      graphChart: null,

      detailGraphData: null,
      detailChart: null,

      graphRelationColors: {},
      graphRelationTypeCounts: [],
      detailRelationColors: {},
      detailRelationTypeCounts: [],

      CHTable: [],
    }
  },
  mounted() {
    this.dataLoading = true;
    this.loadDataAndInitChart();
    this.graphChart = echarts.init(this.$refs.graphChart);
    this.detailChart = echarts.init(this.$refs.detailChart);
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get('/DownFiles-CH.json')
        .then((res) => {
          this.jsonData = res.data.instances
          this.processGraphData()
          this.dataLoading = false
          this.initGraphChart()
        })
        .catch((error) => {
          console.error('Error loading data:', error)
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 CH 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processGraphData() {
      const instances = this.jsonData
      const nodes = []
      const edges = []
      const nodeSet = new Set();

      instances.forEach((instance) => {
        const superType = instance.superType
        const subType = instance.subType

        this.CHTable.push({
          superType: superType.object,
          subType: subType.object,
          relationCount: instance.details.length,
          raw: instance.details,
        })

        if(!nodeSet.has(superType.object)){
          nodes.push({
            id: superType.object,
            name: superType.object,
            category: 'superType',
            file: superType.file,
            location: superType.location,
          });
          nodeSet.add(superType.object);
        }
        
        if(!nodeSet.has(subType.object)){
          nodes.push({
            id: subType.object,
            name: subType.object,
            category: 'subType',
            file: subType.file,
            location: subType.location,
          });
          nodeSet.add(subType.object);
        }

        edges.push({
          source: superType.object,
          target: subType.object,
          value: 'CH',
        })

        edges.push({
          source: subType.object,
          target: superType.object,
          value: 'inherit',
        })
      })
      this.graphRelationTypeCounts = ['CH', 'inherit']

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

      this.graphRelationTypeCounts.forEach((type) => {
        this.graphRelationColors[type] = colors[colorIndex % colors.length]
        colorIndex++
      })

      this.graphData = { nodes, edges }
    },
    initGraphChart() {
      const option = {
        title: {
          text: '循环层次结构可视化',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000',
            fontSize: '1.25rem',
          },
        },
        tooltip: {
          formatter: (param) => {
            if (param.dataType === 'node') {
              return `
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${param.data.name}</span><br>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${param.data.category}</span><br>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${param.data.file}\n(${param.data.location.startLine}-${param.data.location.endLine})</span>
                `
            }
            if (param.dataType === 'edge') {
              return `
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">关系类型：${param.data.value}</span><br>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">起始节点：${param.data.source}</span><br>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">目标节点：${param.data.target}</span>
                `
            }
          },
        },
        toolbox: {
          itemSize: 20,
          iconStyle: {
            borderColor: '#000',
          },
          feature: {
            dataZoom: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            dataView: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            restore: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            saveAsImage: {
              iconStyle: {
                borderColor: '#000',
              },
            },
          },
        },
        legend: {
          data: ['superType', 'subType'],
          bottom: 10,
          textStyle: {
            fontSize: 15,
            color: '#000',
          },
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 200, // 设置节点之间的排斥力，值越大，节点越远
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
              curveness: 0.2,
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
                color: '#000',
              },
            },
            data: this.graphData.nodes,
            links: this.graphData.edges.map((link) => ({
              ...link,
              lineStyle: {
                color: this.graphRelationColors[link.value],
                width: 2,
              },
            })),
            categories: [{ name: 'superType' }, { name: 'subType' }],
          },
        ],
      }

      // 设置图表选项
      this.graphChart.setOption(option)
    },
    viewDetails(row) {
  const details = row.raw;
  const nodes = new Map();
  const edgesMap = new Map(); // 用于去重和统计关系次数
  const relationCount = new Set();

  details.forEach((link) => {
    // 添加或更新 fromEntity 节点信息
    if (!nodes.has(link.fromEntity)) {
      nodes.set(link.fromEntity, {
        id: link.fromEntity,
        name: link.fromEntity,
        category: '父类方法',
        file: link.fromEntityFile,
        location: [],
        depLocation: [],
      });
    }
    const fromNode = nodes.get(link.fromEntity);
    fromNode.location.push(
      `${link.fromEntityLocation.startLine}-${link.fromEntityLocation.endLine}`
    );
    fromNode.depLocation.push(
      `${link.depLocation.startLine}-${link.depLocation.endLine}`
    );

    // 添加 toEntity 节点信息
    nodes.set(link.toEntity, {
      id: link.toEntity,
      name: link.toEntity,
      category: '子类方法',
      file: link.toEntityFile,
      location: [],
    });
    const toNode = nodes.get(link.toEntity);
    toNode.location.push(
      `${link.toEntityLocation.startLine}-${link.toEntityLocation.endLine}`
    );

    // 构建关系键值，用于去重
    const edgeKey = `${link.fromEntity}-${link.toEntity}-${link.relationType}`;
    if (!edgesMap.has(edgeKey)) {
      edgesMap.set(edgeKey, {
        source: link.fromEntity,
        target: link.toEntity,
        value: `${link.relationType} 1`, // 初始关系值，后续会更新
        count: 1, // 初始计数
      });
    } else {
      // 更新已存在的关系
      const edge = edgesMap.get(edgeKey);
      edge.count += 1;
      edge.value = `${link.relationType} ${edge.count}`; // 更新关系计数
    }
    relationCount.add(link.relationType);

    // 添加反向关系
    const inheritKey = `${link.toEntity}-${link.fromEntity}-inherit`;
    if (!edgesMap.has(inheritKey)) {
      edgesMap.set(inheritKey, {
        source: link.toEntity,
        target: link.fromEntity,
        value: 'inherit',
        count: 1,
      });
    }
    relationCount.add('inherit');
  });

  // 将 depLocation 合并为字符串
  nodes.forEach((node) => {
    if (node.location) {
      node.location = node.location.join('; ');
    }
    if (node.depLocation) {
      node.depLocation = node.depLocation.join('; ');
    }
  });

  this.detailRelationTypeCounts = relationCount;

  // 定义关系颜色
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
  ];
  let colorIndex = 0;
  this.detailRelationTypeCounts.forEach((type) => {
    this.detailRelationColors[type] = colors[colorIndex % colors.length];
    colorIndex++;
  });

  // 转换 Map 为数组
  const nodeList = Array.from(nodes.values());
  const edges = Array.from(edgesMap.values()).map((edge) => ({
    ...edge,
    lineStyle: {
      color: this.detailRelationColors[edge.value.split(' ')[0]],
      width: 2,
    },
  }));

  this.graphData = { nodes, edges };
  this.detailGraphData = { nodeList, edges };
  this.initDetailChart();
},
    initDetailChart() {
      const { nodeList, edges } = this.detailGraphData

      const option = {
        title: {
          text: '类的循环层次结构可视化',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000',
            fontSize: '1.25rem',
          },
        },
        tooltip: {
          formatter: (param) => {
            if (param.dataType === 'node') {
              if (param.data.category === '父类方法') {
                return `
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${param.data.name}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${param.data.category}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${param.data.file}\n(${param.data.location})</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">父类调用位置：${param.data.depLocation}</span>
                  `
              } else {
                return `
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${param.data.name}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${param.data.category}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${param.data.file}\n(${param.data.location})</span>
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
            borderColor: '#000',
          },
          feature: {
            dataZoom: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            dataView: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            restore: {
              iconStyle: {
                borderColor: '#000',
              },
            },
            saveAsImage: {
              iconStyle: {
                borderColor: '#000',
              },
            },
          },
        },
        legend: {
          data: ['父类方法', '子类方法'],
          bottom: 10,
          textStyle: {
            fontSize: 15,
            color: '#000',
          },
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 200, // 设置节点之间的排斥力，值越大，节点越远
              gravity: 0.05, // 调整引力，值越大，节点越靠近
              edgeLength: [50, 300], // 控制连接边的长度
            },
            symbolSize: 40, // 节点大小
            roam: true, // 支持鼠标缩放和平移
            label: {
              show: false,
              // position: 'right',
              // formatter: '{b}',
            },
            edgeSymbol: ['none', 'arrow'], // 连接线的两端使用的符号
            edgeSymbolSize: [4, 10], // 连接线符号的大小
            lineStyle: {
              // 连接线的样式配置
              width: 1,
              curveness: 0.2,
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
                color: '#000',
              },
            },
            data: nodeList,
            links: edges.map((link) => ({
              ...link,
              lineStyle: {
                color: this.detailRelationColors[link.value],
                width: 2,
              },
            })),
            categories: [{ name: '父类方法' }, { name: '子类方法' }],
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
    if (this.graphChart) {
      this.graphChart.dispose()
    }
    if (this.detailChart) {
      this.detailChart.dispose()
    }
  },
}
</script>

<style scoped>
.CH {
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
  height: 100%;
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
