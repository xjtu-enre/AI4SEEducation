<template>
  <div class="Architecture">
    <el-tabs v-model="activeName" style="margin-top: 1%; color: black">
      <el-tab-pane label="整体关系概览" name="first">
        <div class="tab-card" style="flex-direction: column;">
          <div class="tab-card-top">
            <p>项目名: {{ fileInfo.folderName }}</p>
            <p>项目大小: {{ fileInfo.literallySize }}</p>
            <p>包含文件数: {{ fileInfo.files.length }}</p>
          </div>
          <div class="tab-card-bottom">
            <div class="tab-card-bottom-top">
              <p><strong>项目列表：</strong></p>
            </div>
            <div class="tab-card-bottom-bottom">
              <el-tree :data="fileTreeData" :props="defaultProps" :highlight-current="true"></el-tree>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="全局依赖关系概览" name="second">
          <el-card>
              <div class="tab-card">
                  <div class="entity-relation-table">
                      <div class="entity-table">
                          <div class="entity-relation-table-title">
                              <strong>实体类型分布</strong>
                          </div>
                          <el-table 
                              :data="entityNumList"
                              highlight-current-row
                              height="90%"
                              :cell-style="getCellStyle"
                              :header-cell-style="getHeaderStyle"
                          >
                              <el-table-column prop="name" label="实体类型"></el-table-column>
                              <el-table-column prop="value" label="数量" sortable></el-table-column>
                          </el-table>
                      </div>

                      <div class="relation-table">
                          <div class="entity-relation-table-title">
                              <strong>关系类型分布</strong>
                          </div>
                          <el-table 
                              :data="relationNumList"
                              highlight-current-row
                              height="95%"
                              :cell-style="getCellStyle"
                              :header-cell-style="getHeaderStyle"
                          >
                              <el-table-column prop="name" label="关系类型"></el-table-column>
                              <el-table-column prop="value" label="数量" sortable></el-table-column>

                          </el-table>
                      </div>
                  </div>
                  <div class="tree-list">
                      <div class="entity-relation-table-title">
                          <strong>依赖关系层次结构</strong>
                      </div>
                      <div style="height: 90%;overflow: auto;">
                          <el-tree 
                              :data="treeData"
                              :props="defaultProps"
                              node-key="id"
                          >
                              <template v-slot="{ data }">
                                  <span>
                                  <span :style="{ color: data.color }" style="font-size: 1.1rem">{{ data.name }}</span>
                                  <span style="margin-left: 10px; font-size: 1rem; color: #666;">({{ data.category }})</span>
                                  </span>
                              </template>
                          </el-tree>
                      </div>
                  </div>
              </div>
          </el-card>
      </el-tab-pane>

      <el-tab-pane label="全局依赖关系图" name="third">
          <el-card>
              <div class="entity">
                  <div class="cytoscape-png"></div>
                  <el-tag type="warning" style="margin-right: 1%;font-size: 1.1rem;">注：点击红框区域可关闭已展开节点</el-tag>
                  <button icon="el-icon-refresh" class="btn" @click="getEntity">重新加载</button>
                  <div id="network1" style="margin-top: 1%;"></div>
              </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="项目指标结果" name="forth">
        <el-card>
            <!-- <div class="Index">
                <div class="cytoscape-png"></div>
                <div id="network2" style="margin-top: 1%;"></div>
            </div> -->
            
              <div class="tab-card">
                  <div class="entity-relation-table">
                    <div class="metric-section-title">
                          <strong>项目级指标雷达图</strong>
                      </div>
                    <div class="category-button-group" style="margin-top: 4px;">
                      <el-button
                        class="category-button"
                        :class="{ active: currentCategory === 'coupling' }"
                        @click="switchCategory('coupling')"
                      >耦合</el-button>

                      <el-button
                        class="category-button"
                        :class="{ active: currentCategory === 'cohesion' }"
                        @click="switchCategory('cohesion')"
                      >内聚</el-button>

                      <el-button
                        class="category-button"
                        :class="{ active: currentCategory === 'inheritance' }"
                        @click="switchCategory('inheritance')"
                      >继承</el-button>

                      <el-button
                        class="category-button"
                        :class="{ active: currentCategory === 'encapsulation' }"
                        @click="switchCategory('encapsulation')"
                      >封装</el-button>
                    </div>
                    
                     <div ref="chart" style="width: 400px; height: 400px;"></div>
                     <div class="category-description">
                        <p v-if="currentCategory === 'coupling'">
                          <strong>耦合：</strong>衡量模块间依赖关系的紧密程度，如 CBO（对象间的耦合）表示一个类调用和被调用的总数。
                        </p>
                        <p v-else-if="currentCategory === 'cohesion'">
                          <strong>内聚：</strong>评估模块内部元素的功能相关性，如 LCOM（方法内聚性缺失）反映类中方法共享字段的分散程度。
                        </p>
                        <p v-else-if="currentCategory === 'inheritance'">
                          <strong>继承：</strong>分析类层次结构的复杂度，如 DIT（继承树深度）表示从基类到当前类的层级数。
                        </p>
                        <p v-else-if="currentCategory === 'encapsulation'">
                          <strong>封装：</strong>检测数据隐藏和访问控制的有效性，如 AHF（属性隐藏因子）衡量私有/保护属性占总属性的比例。
                        </p>
                      </div>
                    
                  </div>
                  <div class="metricTreeList">
                      <div class="metric-section-title">
                          <strong>指标层次结构</strong>
                      </div>
                      <div style="height: 90%;overflow: auto;">
                          <el-tree 
                              :data="metricTreeData"
                              :props="defaultProps"
                              
                          >
                              <template v-slot="{ data }">
                                  <span>
                                  <span :style="{ color: '#000' }" style="font-size: 1.1rem">{{ data.name }}</span>
                                  <!-- <span style="margin-left: 10px; font-size: 1rem; color: #666;">({{ data.category }})</span> -->
                                  </span>
                              </template>
                          </el-tree>
                      </div>
                  </div>
              </div>
          </el-card>
      
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
// @ts-ignore
import cytoscape from "cytoscape";
import fcose from 'cytoscape-fcose';
import { getJsonResult, resultUrls } from '../../services/analysisResults';
import * as echarts from 'echarts';
import EventBus from "../eventBus";

export default {
  name: 'Architecture',
  data() {
    return {
      activeName: 'first',

      fileInfo: {
          folderName: '请先上传项目',
          totalSize: 0,
          literallySize: 0,
          files: [],
      },
      treeData: [],
      fileTreeData: [], // 存储文件夹树形结构

      rawMetricData: {},
      metricTreeData: [],

      chart: null,
      currentCategory: 'coupling',
      categories: {
        coupling: ['PC', 'DL', 'ODD', 'IDD', 'CF'],
        cohesion: ['CHM', 'CHD'],
        inheritance: ['MIF', 'AIF', 'PF'],
        encapsulation: ['MHF', 'AHF']
      },

      defaultProps: {
          label: 'label',
          children: 'children'
      },

      entityNumList: [],
      relationNumList: [],

      graphLoading: false,
      childIdMap: new Map(),
      parentIdMap: new Map(),
      fileData: [],
      entityRoot: [],
      entityIdMap: new Map(),
      treeRoot: undefined,
      showedIds: new Set(),
    }
  },
  created() {
    this.getEntity();
  },
  mounted() {
    this.chart = echarts.init(this.$refs.chart);
    this.updateChart(); 
  },
  // watch: {
  //   chartIndex(){
  //     this.renderChart();
  //   }
  // },
  methods: {
    handleFileInfo(fileInfo){
      this.fileInfo = fileInfo

      let files = fileInfo.files
      const fileMap = new Map();

      // 遍历所有文件
      for (const file of files) {
          const relativePath = file.webkitRelativePath; // 获取文件相对路径
          const pathParts = relativePath.split('/'); // 拆分路径获取文件层级

          let currentLevel = fileMap;

          // 遍历路径的每一层
          for (let i = 0; i < pathParts.length; i++) {
            const part = pathParts[i];

            if (!currentLevel.has(part)) {
                currentLevel.set(part, new Map());
            }

            // 进入下一层
            currentLevel = currentLevel.get(part);
          }
      }

      // 递归构建树形结构
      this.fileTreeData = this.buildTree(fileMap);
    },
    buildTree(map) {
        return [...map.entries()].map(([key, value]) => ({
            label: key,
            children: this.buildTree(value)
        }));
    },
    getEntityCategory(node) {
      if (node["External"] === true) {
        return "External";
      }
      return node["category"];
    },
    getRelationCategory(cell) {
      for (const key of Object.keys(cell["values"])) {
        if (key === "loc" || key === "bindVar" || key === "modifyAccessible" || key === "invoke" || key === "arguments") {
          continue;
        }
        return key;
      }
      return undefined;
    },
    getRelationsByIds(entityIds) {
      const relations = []
      if (!this.data.cells) {
        return undefined;
      }
      for (const cell of this.data["cells"]) {
        if (entityIds.has(cell["src"]) && entityIds.has(cell["dest"])) {
          relations.push(cell);
        }
      }
      return relations;
    },
    getChildrenById(entityId) {
      if (!this.parentIdMap.has(entityId)) {
        return [];
      }
      return this.parentIdMap.get(entityId);
    },
      async getData() {
        const [enreData, metricData] = await Promise.all([
          getJsonResult(resultUrls.upstreamEnre),
          getJsonResult(resultUrls.metricsPre)
        ]);
        this.data = enreData;
        this.rawMetricData = metricData["modules"];

            // 实体类型分布表
            this.entityNumList = Object.entries(this.data.entityNum).map(([name, value]) => ({
                name,
                value
            }));
            this.entityNumList = [...this.entityNumList].sort((a, b) => b.value - a.value);
            // 关系类型分布表
            this.relationNumList = Object.entries(this.data.relationNum).map(([name, value]) => ({
                name,
                value
            }));
            this.relationNumList = [...this.relationNumList].sort((a, b) => b.value - a.value);

            const upData = {
                entity: this.entityNumList,
                relation: this.relationNumList
            }
            
            EventBus.upData = upData

            for (const variable of this.data["variables"]) {
                this.entityIdMap.set(variable["id"], variable);
                if (!this.parentIdMap.has(variable["parentId"])) {
                    this.parentIdMap.set(variable["parentId"], []);
                }
                this.parentIdMap.get(variable["parentId"]).push(variable);
            }

            // 树列表
            let tree = [];
            let map = {};
            // 先创建 id -> 节点的映射
            let jsonData1 = this.data["variables"]
            jsonData1.forEach(item => {
                map[item.id] = { 
                    ...item, 
                    category: item.category === "item" ? "file" : item.category, // 替换 category
                    color: item.color ? item.color : "black",
                    children: [] 
                };
            });

            // 组装树结构
            jsonData1.forEach(item => {
                if (item.parentId === -1) {
                    tree.push(map[item.id]); // 根节点
                } else {
                    if (map[item.parentId]) {
                        map[item.parentId].children.push(map[item.id]); // 追加到父节点的 children
                    }
                }
            });

            this.treeData = tree;

            let metricTree = [];
            let id = 0
            // console.log(metricData);
            // 遍历根层级（包名）
            Object.entries(this.rawMetricData).forEach(([moduleName, moduleData]) => {
              const moduleNode = {
                name: moduleName,  // 包名作为节点标签
                children: [],  // 初始没有子节点
                id: id++
              };

              // 遍历包下的所有属性
              Object.entries(moduleData).forEach(([moduleMetricKey, moduleMetricVal]) => {
                // 如果是 classes 属性，则继续处理类
                if (moduleMetricKey === 'classes') {
                  Object.entries(moduleMetricVal).forEach(([className, classData]) => {
                    const classNode = {
                      name: className,  // 类名作为节点标签
                      children: [],  // 类的子节点
                      id: id++
                    };

                    
                    
                    Object.entries(classData).forEach(([clsMetricKey, clsMetricVal]) => {
                      
                      // 第三级
                      if(clsMetricKey === 'methods'){
                        Object.entries(clsMetricVal).forEach(([methodName, methodData]) => {
                          const methodNode = {
                            name: methodName,
                            children: Object.entries(methodData).map(([metricKey, metricVal]) => ({
                              name: `${metricKey}: ${metricVal}`,
                              children: [],  // 方法节点不含有子节点，只有度量值
                              id: id++
                            }))
                          };
                          classNode.children.push(methodNode);
                        });
                        
                      } else {
                        classNode.children.push({
                          name: `${clsMetricKey}: ${clsMetricVal}`,
                          children: [],
                          id: id++
                        })
                      }

                      // 将方法添加为类的子节点
                      //classNode.children.push(methodNode);
                    });
                    

                    // 将类添加为包的子节点
                    moduleNode.children.push(classNode);
                  });
                } else {
                  moduleNode.children.push({
                    name: `${moduleMetricKey}: ${moduleMetricVal}`,
                    children: [],
                    id: id++
                  })
                  // 如果是其他属性（如 DSC，scoh 等），直接作为包的子节点
                  // const otherNode = {
                  //   label: `${metricKey}: ${metricVal}`,  // 显示属性名和属性值
                  //   children: []  // 不含子节点
                  // };

                  // moduleNode.children.push(otherNode);
                }
              });

              // 将包添加到最终的树形数据
              metricTree.push(moduleNode);
            });

            this.metricTreeData = metricTree;

      },
    async getEntity() {
      try {
        await this.getData();
        console.log('Data loaded:', this.data);
      } catch (error) {
        console.error('Error fetching data:', error);
      }

      const ref = this;
      this.entityRoot = []
      let edgesParam = new Set();
      this.getChildrenById(-1).forEach((node) => {
        const cat = ref.getEntityCategory(node);
        this.entityRoot.push({
          group: 'nodes',
          data: {id: node["id"], name: node["name"], parent: -1, classes: 'center-center', category: cat}
        });
        edgesParam.add(node["id"]);
      })
      let relations = ref.getRelationsByIds(edgesParam);
      if (relations !== undefined) {
        ref.getRelationsByIds(edgesParam).forEach((edge) => {
          const cat = ref.getRelationCategory(edge);
          if (cat !== 'Contain' && cat !== 'Define') {
            this.entityRoot.push({
              group: 'edges',
              data: {id: edge.id, source: String(edge["src"]), target: String(edge["dest"]), category: cat}
            });
          }
        });
      }
      // let cytoscape = window.cytoscape; node.id+'\n'+node.entityName
      /* eslint-disable */
      cytoscape.use(fcose);
      let cy = cytoscape({
        container: document.getElementById('network1'),
        ready: function () {
          this.nodes().forEach(function (node) {
            let size = 50;
            node.css("width", size);
            node.css("height", size);
          });
          // this.layout({name: 'fcose', fit: true, nodeRepulsion: 99999,initialEnergyOnIncremental: 0.1,nestingFactor:0.1, animationEasing: 'ease-out'}).run();
        },
        layout: {
          name: "fcose",
          fit: true,
          nodeRepulsion: 99999,
          animationDuration: 300,
          spacingFactor: 2.4,
          nodeDimensionsIncludeLabels: false,
        },
        // zoomingEnabled: false,
        // userZoomingEnabled: false,
        maxZoom: 1,
        minZoom: 1,
        style: [
          {
            selector: 'node',
            style: {
              'label': 'data(name)',
              'shape': 'data(type)',
              'color': 'data(color)',
              "text-wrap": "wrap",
              'font-size': '14px',
              'background-opacity': 0.6,
              'background-color': '#2B65EC'
            }
          },

          {
            selector: ':parent',
            style: {
              'background-opacity': 0.2,
              'border-color': '#62f'
            }
          },

          {
            selector: 'edge',
            style: {
              'label': 'data(category)',
              'line-color': '#2B65EC'
            }
          },

          {
            selector: 'node:selected',
            style: {
              'background-color': '#F08080',
              'border-color': 'red'
            }
          },

          {
            selector: 'edge:selected',
            style: {
              'line-color': '#F08080'
            }
          }
        ],
        elements: this.entityRoot,
      });
      console.log('Cytoscape instance:', cy); // 打印 Cytoscape 实例信息
      console.log('Entity Root:', this.entityRoot);
      cy.on('tap', 'node', function (evt) {
        var target = evt.target;
        console.log("target");
        console.log(target);
        if (target.selected()) {
          target.children().forEach(ele => {
            cy.remove(ele)
          });

          cy.remove(target);
          cy.add(target);
          // cy.add({group:target.group, data:{id:target.entityId, name: target.entityId+'\n'+target.entityName, type: 'nodes', parent: target.parent}})
        } else {
          let edgesParam = new Set();
          let relations = ref.getChildrenById((Number(target.id())));
          if (relations !== undefined){
            ref.getChildrenById(Number(target.id())).forEach((node) => {
              const cat = ref.getEntityCategory(node);
              cy.add({
                group: 'nodes',
                data: {
                  id: node["id"],
                  name: node["name"],
                  parent: target.id(),
                  type: ref.getShape(cat),
                  classes: 'center-center',
                  category: cat
                }
              })
              cy.nodes().forEach((node) => {
                let size = 50;
                node.css("width", size);
                node.css("height", size);
              });
            })
          }
          cy.elements().forEach(ele => {
            edgesParam.add(ele.data().id)
          })
          relations = ref.getRelationsByIds(edgesParam);
          if (relations !== undefined){
            ref.getRelationsByIds(edgesParam).forEach((edge) => {
              const cat = ref.getRelationCategory(edge);
              if (cat !== 'Contain' && cat !== 'Define') {
                cy.add({
                  group: 'edges',
                  data: {id: edge.id, source: String(edge["src"]), target: String(edge["dest"]), category: cat}
                })
              }
            });
          }
          let layout = cy.layout({
            name: "fcose",
            fit: true,
            nodeRepulsion: 999, // 节点排斥
            randomize: false,
            animationDuration: 300,
            padding: 30,
            nodeDimensionsIncludeLabels: false, // 标签包含文字
            initialEnergyOnIncremental: 0.5, // 初始能量增量
            nestingFactor: 0.5, // 嵌套因素
            spacingFactor: 2, // 间距因素
          })
          layout.run()
        }
      });
      // cy.nodes().on('right-click', (evt) => {
      //     console.log(evt.target)
      // });
      // cy.edges().on('click', (evt) => {
      //     console.log(evt.target)
      // });
    },

    async getIndex() {

    },
    existSourceTarget(id) {
      console.log(id)
    },
    getShape(cate) {
      if (cate == 'File') {
        return 'cut-rectangle'
      } else if (cate == 'Class') {
        return 'round-triangle'
      } else if (cate == 'Enum') {
        return 'pentagon'
      } else if (cate == 'Enum Constant') {
        return 'star'
      } else if (cate == 'Annotation') {
        return 'right-rhomboid'
      } else if (cate == 'Annotation Member') {
        return 'rhomboid'
      } else if (cate == 'Interface') {
        return 'diamond'
      } else if (cate == 'Method') {
        return 'concave-hexagon'
      } else if (cate == 'Module') {
        return 'round-tag'
      } else if (cate == 'Type Parameter') {
        return 'hexagon'
      } else if (cate == 'Variable') {
        return 'vee'
      } else {
        return 'ellipse'
      }
    },
      getHeaderStyle() {
          return {
              backgroundColor: 'transparent',
              color: '#000', // 设置表头文字为黑色
              textAlign: 'center',
              fontWeight: 'bold', // 表头加粗
          }
      },
      getCellStyle() {
          return {
              color: '#000', // 设置单元格文字为黑色
              textAlign: 'center',
              border: 'none', // 去掉单元格边框
          }
      },
      switchCategory(cat) {
      this.currentCategory = cat;
      this.updateChart();
    },
    updateChart() {
      const metrics = this.categories[this.currentCategory];
      const indicators = metrics.map((m) => ({
        name: m,
        max: Math.max(1, metricData[m] * 1.2 || 1)
      }));
      const values = metrics.map((m) => metricData[m] || 0);

      const option = {
        // title: { text: `雷达图 - ${this.currentCategory}` },
        tooltip: {},
        radar: {
          indicator: indicators
        },
        series: [
          {
            type: 'radar',
            data: [
              {
                value: values,
                name: '指标值'
              }
            ]
          }
        ]
      };

      this.chart.setOption(option);
    }
  }
}
</script>

<style scoped>

.Architecture {
  width: 100%;
  height: 100%;
}

.title-header {
  margin: 0.5% 0 0.5% 0;
  color: black;
  font-size: 1.2rem;
}

.over-view {
  width: max(50vw, 800px);
}

#network1 {
  width: 100%;
  height: max(65vh, 800px);
  overflow: visible;
  border: 1px solid #69f;
  position: relative; /* 确保定位正确 */
  z-index: 10; /* 确保内容不会被其他元素遮挡 */
  text-align: start;
}

#callgraph {
  width: 100%;
  height: 350px;
  overflow: auto;
  text-align: center;
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #69f;
}

.tab-card {
  width: 100%;
  height: 100%;
  display: flex;
}

.tab-card-top {
  height: 50px;
  width: 100%;
  margin-left: 2%;
  display: flex;
  /* flex-direction: column;*/
}

.tab-card-top p {
  text-align: left;
  font-size: 1.1rem;
  padding-bottom: 0.2%;
  width: 30%;
}

.tab-card-bottom {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 900px;
  margin-left: 2%;
}

.tab-card-bottom-top {
  display: flex;
  width: 100%;
  height: 50px;
  font-size: 1.1rem;
}

.tab-card-bottom-bottom {
  display: flex;
  width: 100%;
  height: 850px;
  overflow: auto;
  /* align-items: center; */
  justify-content: center;
}

.entity-relation-table {
  width: 50%;
  height: 900px;
  display: flex;
  flex-direction: column;
  padding: 1%;
  gap: 3%;
}

.tree-list {
    width: 50%;
    height: 900px;
}
.metricTreeList{
  width: 50%;
    height: 900px;
    display: flex;
    flex-direction: column;
    padding: 1%;
    gap: 3%;
}
.category-button-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 每行两个按钮 */
  gap: 16px; /* 行列间距 */
  justify-items: center; /* 居中每个按钮 */
  margin: 12px 0 20px;
}

.category-button {
  color: #000 !important;
  background-color: #f5f5f5;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  min-width: 100px;
  text-align: center;
  transition: background-color 0.3s ease;
}

.category-button:hover {
  background-color: #e0e0e0;
  color: #000;
}

.category-button.active {
  background-color: #4CAF50 !important; /* Element UI 默认主题蓝 */
  color: white !important;
  border-color: #4CAF50 !important;
}
.category-description {
  margin-top: 10px;
  font-size: 14px;
  color: #333;
  width: 400px;
  text-align: left;
}
.category-description p {
  margin: 5px 0;
  line-height: 1.6;
}
.entity-table {
  display: flex;
  flex-direction: column;
  height: 460px;
}

.relation-table {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.entity-relation-table-title {
  height: 8%;
  width: 100%;
  font-size: 1.25rem;
}
.metric-section-title {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 8%;
  width: 100%;
  font-size: 1.25rem;
  text-align: center;
  margin-top: -50px; /* 往上移一点，可根据需要调整数值 */
}
::v-deep .el-tabs__header {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

::v-deep .el-tabs__nav-wrap {
  display: flex;
  justify-content: center;
}

::v-deep .el-tabs__nav {
  float: none;
}

::v-deep .el-tabs__item {
  font-size: 24px;
  padding: 0 50px;
}

::v-deep .el-card {
  background-color: transparent;
}

::v-deep .el-tree-node__content {
color: black !important;
font-size: 1.25rem
}

::v-deep .el-tree-node__label {
color: black !important;
font-size: 1.25rem;
}

::v-deep .el-tree__empty-text{
position: relative;
top: 0;
}
</style>
