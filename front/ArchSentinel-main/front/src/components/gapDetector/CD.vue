<template>
  <div
    class="CD"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="main-table">
        <el-table
          :data="CDTable"
          highlight-current-row
          height="100%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column prop="modules" label="存在循环依赖的包">
            <template slot-scope="scope">
              {{ scope.row.modules.join('\n') }}
            </template>
          </el-table-column>
          <el-table-column
            prop="moduleCount"
            label="包的数量"
            width="200"
            sortable
          ></el-table-column>
          <el-table-column
            prop="dependencyRelation"
            label="依赖关系"
            width="200"
          >
            <template slot-scope="scope">
              <div
                v-for="(value, key) in scope.row.dependencyRelation"
                :key="key"
              >
                {{ key }}: {{ value.value }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="dependencyRelationCountTotal"
            label="依赖关系总数"
            width="200"
            sortable
          ></el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <button class="btn" @click="viewDetails(scope.row)">
                查看详情
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="main-chart">
        <div style="height: 90%; width: 40%">
          <el-table
            :data="detailTable"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
          >
            <el-table-column
              prop="sourcePackage"
              label="包名"
            ></el-table-column>
            <el-table-column
              prop="targetPackage"
              label="所依赖包名"
            ></el-table-column>
            <el-table-column
              prop="outDegree"
              label="出度"
              width="80"
            ></el-table-column>
            <el-table-column prop="relation" label="依赖关系"></el-table-column>
            <el-table-column label="操作">
              <template slot-scope="scope">
                <button class="btn" @click="viewRelation(scope.row)">
                  查看详情
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div ref="detailChart" style="height: 90%; width: 30%"></div>
        <div ref="relationChart" style="height: 90%; width: 30%"></div>
      </div>
    </div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 CD 文件</span>
    </div>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'CD',
  data() {
    return {
      fileFind: true,

      jsonData: null,
      dataLoading: false,

      CDTable: [],

      detailTable: [],
      detailChart: null,
      relationChart: null,

      topNodeColorMap: null,
    }
  },
  mounted() {
    this.dataLoading = true
    this.loadDataAndInitChart()
    this.detailChart = echarts.init(this.$refs.detailChart)
    this.relationChart = echarts.init(this.$refs.relationChart)
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get('/DownFiles-CD.json')
        .then((res) => {
          this.jsonData = res.data.instances
          this.processTableData()
          this.dataLoading = false
        })
        .catch((error) => {
          console.error('Error loading data:', error)
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 CD 文件',
              type: 'warning'
            })
            return
          }
        })
    },
    processTableData() {
      this.CDTable = this.jsonData.map((cd) => ({
        modules: cd.modules,
        moduleCount: cd.moduleCount,
        dependencyRelation: cd.dependencyRelationCountTotal,
        dependencyRelationCountTotal: Object.values(
          cd.dependencyRelationCountTotal
        ).reduce((sum, count) => sum + count.value, 0),
        raw: cd,
      }))
    },
    viewDetails(row) {
      this.initDetailTable(row)
      this.initDetailChart(row)
    },
    initDetailTable(row) {
      let table = []
      if (row.raw) {
        row.raw.packageDependencyRelationCells.forEach((cell) => {
          const target = cell.targetPackage.join('\n')
          const relation = Object.entries(
            cell.dependencyRelationCountFromSrcToAllDest
          )
            .map(([key, val]) => `${key}: ${val.value}`)
            .join('\n')
          table.push({
            sourcePackage: cell.sourcePackage,
            targetPackage: target,
            outDegree: cell.outDegree,
            relation: relation,
            raw: cell.mutualPackageDependencyRelations,
          })
        })
      }
      this.detailTable = [...table]
    },
    initDetailChart(row) {
      let nodes = []
      let links = []
      const nodeSet = new Set()
      const topNodeColorMap = new Map(); // 顶层节点到颜色的映射
      const colorPalette = [
        '#FF5733', '#33FF57', '#3357FF', '#FFC300', '#DAF7A6', '#FF33C4',
        '#33A6FF', '#9B59B6', '#E74C3C', '#1ABC9C', '#8E44AD', '#FFA633',
      ];
      let colorIndex = 0;

      if (row.raw) {
        row.raw.packageDependencyRelationCells.forEach((cell) => {
          // 为 source 分配颜色
          if (!topNodeColorMap.has(cell.sourcePackage)) {
            topNodeColorMap.set(cell.sourcePackage, colorPalette[colorIndex % colorPalette.length]);
            colorIndex += 1;
          }
          const sourceColor = topNodeColorMap.get(cell.sourcePackage);

          // 添加 source 节点
          if (!nodeSet.has(cell.sourcePackage)) {
            nodes.push({
              name: cell.sourcePackage,
              outDegree: cell.outDegree,
              itemStyle: { color: sourceColor },
            })
            nodeSet.add(cell.sourcePackage)
          }

          // 添加 target 节点
          cell.targetPackage.forEach((target) => {
            // 如果 target 是新的顶层节点，分配新的颜色
            if (!topNodeColorMap.has(target)) {
              topNodeColorMap.set(target, colorPalette[colorIndex % colorPalette.length]);
              colorIndex += 1;
            }
            const targetColor = topNodeColorMap.get(target);

            if (!nodeSet.has(target)) {
              nodes.push({
                name: target,
                itemStyle: { color: targetColor }, // 设置颜色
              })
              nodeSet.add(target)
            }
            // 添加链接
            links.push({
              source: cell.sourcePackage,
              target: target,
              value: cell.dependencyRelationCountFromSrcToAllDest,
              details: cell.mutualPackageDependencyRelations, // 存储链接详情
            })
          })
        })
      }

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
      const sourceColorMap = new Map() // 保存 sourcePackage -> color 的映射
      colorIndex = 0

      // 遍历 links 数据，设置颜色
      links.forEach((link) => {
        const source = link.source
        if (!sourceColorMap.has(source)) {
          // 如果没有为该 sourcePackage 分配颜色，从调色板中取一个
          sourceColorMap.set(source, colors[colorIndex % colors.length])
          colorIndex += 1
        }
        link.lineStyle = {
          color: sourceColorMap.get(source), // 为边设置颜色
        }
      })

      const option = {
        title: {
          text: '循环依赖关系图',
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
                                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${param.data.name}</span><br>
                            `
            }
            if (param.dataType === 'edge') {
              return `
                                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">关系类型：${Object.entries(
                                  param.data.value
                                )
                                  .map(([key, val]) => `${key}: ${val.value}`)
                                  .join(', ')}</span><br>
                                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">起始节点：${
                                  param.data.source
                                }</span><br>
                                <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">目标节点：${
                                  param.data.target
                                }</span>
                            `
            }
          },
        },
        toolbox: {
          itemSize: 20,
          top: 10,
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
              formatter: (params) => {
                const value = params.data.value // 获取当前边的 value 数据
                if (value) {
                  return Object.entries(value)
                    .map(([key, val]) => `${key}: ${val.value}`)
                    .join(', ')
                }
                return ''
              },
              textStyle: {
                fontSize: 12,
                color: '#000000',
              },
            },
            data: nodes,
            links: links,
          },
        ],
      }

      this.detailChart.setOption(option);
      this.topNodeColorMap = topNodeColorMap;
    },
    viewRelation(row) {
      const nodes = new Map()
      const edges = []
      const topNodeColorMap = this.topNodeColorMap;

      row.raw.forEach((relation) => {
        relation.entityDependencies.forEach((entityDependency) => {
          const detail = entityDependency.entityDependencyRelationDetail
          if (detail) {
            // 确定 fromEntity 和 toEntity 的顶层节点
            const fromTopNode = this.findTopNode(detail.fromEntity, topNodeColorMap);
            const toTopNode = this.findTopNode(detail.toEntity, topNodeColorMap);

            const fromColor = topNodeColorMap.get(fromTopNode);
            const toColor = topNodeColorMap.get(toTopNode);

            // 添加 fromEntity 节点
            nodes.set(detail.fromEntity, {
              name: detail.fromEntity,
              file: detail.fromEntityFile,
              type: detail.fromEntityType,
              modifier: detail.fromEntityModifier,
              location: detail.fromEntityLocation,
              itemStyle: { color: fromColor }, // 设置颜色
            });

            // 添加 toEntity 节点
            nodes.set(detail.toEntity, {
              name: detail.toEntity,
              file: detail.toEntityFile,
              type: detail.toEntityType,
              modifier: detail.toEntityModifier,
              location: detail.toEntityLocation,
              itemStyle: { color: toColor }, // 设置颜色
            });

            // 添加边信息，关联详细信息
            edges.push({
              source: detail.fromEntity,
              target: detail.toEntity,
              value: detail.relationType,
              details: detail, // 保存详细信息到边上
              depLocation: detail.depLocation,
            });
          }
        })
      })

      const colors = [
        'gray', '#33FF57', '#3357FF', '#FFC300', '#DAF7A6', '#FF33C4',
        '#33FFF4', '#A633FF', '#57FF33', '#FF33A6', '#33A6FF', '#FFA633',
      ];

      const valueColorMap = new Map();
      let colorIndex = 0;

      // 遍历 links 数据，设置颜色
      edges.forEach((link) => {
        const value = link.value;
        if (!valueColorMap.has(value)) {
          valueColorMap.set(value, colors[colorIndex % colors.length]);
          colorIndex += 1;
        }
        link.lineStyle = {
          color: valueColorMap.get(value),
        };
      });

      const nodeData = Array.from(nodes.values());

      const option = {
        title: {
          text: '循环依赖关系子图',
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
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">节点名称：${param.data.name}</span>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">修饰符：${param.data.modifier}</span>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">类型：${param.data.type}</span>
                    <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">位置：${param.data.file}\n(${param.data.location.startLine}-${param.data.location.endLine})</span>
                `
            }
            if (param.dataType === 'edge') {
              return `
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">关系类型：${param.data.value}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">起始节点：${param.data.source}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">目标节点：${param.data.target}</span>
                      <span style="display: flex;font-size: 1.25rem;margin-bottom: 1%;">关键位置：${param.data.depLocation.startLine}-${param.data.depLocation.endLine}</span>
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
                color: '#000000',
              },
            },
            data: nodeData,
            links: edges,
          },
        ],
      }
      this.relationChart.setOption(option)
    },
    // 查找节点的顶层节点
    findTopNode(nodeName, topNodeColorMap) {
      for (const topNode of topNodeColorMap.keys()) {
        if (nodeName.startsWith(topNode)) {
          return topNode;
        }
      }
      return nodeName; // 如果未找到顶层节点，返回自身作为顶层节点
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
    if (this.detailChart) {
      this.detailChart.dispose()
    }
    if (this.relationChart) {
      this.relationChart.dispose()
    }
  },
}
</script>

<style scoped>
.CD {
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
  height: 45%;
  width: 100%;
  display: flex;
}

.main-chart {
  margin-top: 1%;
  height: 64%;
  width: 100%;
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
