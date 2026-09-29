<template>
  <div class="Architecture">
    <!-- <el-tabs v-model="activeName">
      <el-tab-pane label="整体项目概览" name="first">
        <el-card> -->
          <div class="entity">
            <div class="title-header">全局依赖关系图：</div>
            <div class="cytoscape-png"></div>
            <el-tag type="warning" style="margin-right: 1%;font-size: 1.1rem;">注：点击红框区域可关闭已展开节点</el-tag>
            <button type="text" icon="el-icon-refresh" class="btn" @click="getEntity">重新加载</button>
            <div id="network1" style="margin-top: 1%;"></div>
          </div>
        <!-- </el-card>
      </el-tab-pane>
    </el-tabs> -->
  </div>
</template>

<script>
// @ts-ignore
import cytoscape from "cytoscape";
import fcose from 'cytoscape-fcose'
import { getJsonResult, resultUrls } from '../../services/analysisResults'

export default {
  name: 'Architecture',
  data() {
    return {
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
  methods: {
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
      this.data = await getJsonResult(resultUrls.upstreamEnre);
      for (const variable of this.data["variables"]) {
        this.entityIdMap.set(variable["id"], variable);
        if (!this.parentIdMap.has(variable["parentId"])) {
          this.parentIdMap.set(variable["parentId"], []);
        }
        this.parentIdMap.get(variable["parentId"]).push(variable);
      }
    },
    async getEntity() {
      await this.getData();
      console.log('Data loaded:', this.data);
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
  }
}
</script>

<style scoped>
.entity {
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
</style>
