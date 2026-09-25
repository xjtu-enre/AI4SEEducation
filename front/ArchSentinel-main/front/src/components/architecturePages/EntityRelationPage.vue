<template>
  <div>
  <div class="app-container">
    <div class="header">
      <div class="left-header">
        <router-link :to="`/draw-uml?scanuuid=${scanUuid}`">
          <button class="check-button">Architecture Violation Check</button>
        </router-link>
      </div>

      <div class="right-header">
        <div class="project-discription">{{ 'New Project 123' }}</div>
        <div class="project-discription">{{ formatedTime }}</div>
      </div>
    </div>

    <architecture-check-result-window
      :scanUuid="scanUuid"
    ></architecture-check-result-window>
  </div>

  <div class="bottom">
    <information-table
      :table-title="entityInfoTableTitle"
      :table-heads="entityInfoTableHeads"
      :table-data="entityInfoTableData"
    ></information-table>
  </div>
  </div>
</template>

<script>
import InformationTable from '@/components/architectureComponents/InformationTable.vue'
import ArchitectureCheckResultWindow from '@/components/architectureComponents/ArchitectureCheckResultWindow.vue'
import { useQueryEntitiesByCategory } from '@/components/architectureComponents/script/architecture'

export default {
  data() {
    return {
      generatedTime: new Date(),
      formatedTime: `${new Date().getFullYear()}/${
        new Date().getMonth() + 1
      }/${new Date().getDate()} ${new Date().getHours()}:${new Date()
        .getMinutes()
        .toLocaleString('en-us', {
          minimumIntegerDigits: 2,
          useGrouping: false,
        })}`,
      projectMethodEntityList: [],
      entityInfoTableData: [],
      entityInfoTableTitle: 'Show: Functions',
      entityInfoTableHeads: ['Entity', 'File', 'Line', 'Architecture'],
    }
  },
  mounted() {
    const urlParams = new URLSearchParams(window.location.search)
    const scanUuid = urlParams.get('scanuuid')

    this.methodEntityResult = useQueryEntitiesByCategory(scanUuid, 'Method')
    this.handleMethodEntityResult()
  },
  watch: {
    'methodEntityResult.data': function (newValue) {
      if (newValue) {
        this.projectMethodEntityList = newValue
        console.log('projectMethodEntityList updated')
        this.entityInfoTableData = []
        for (const method of this.projectMethodEntityList) {
          let files = method.file.split('/')
          this.entityInfoTableData.push({
            Entity: method.entityName,
            File: files[files.length - 1],
            Line: method.endLine - method.startLine + 1,
            Architecture: 'Directory Structure: '.concat(method.file),
          })
        }
      }
    },
  },
  methods: {
    handleMethodEntityResult() {
      console.log('methodEntityResult = ', this.methodEntityResult)
      if (this.methodEntityResult.data) {
        this.projectMethodEntityList = this.methodEntityResult.data
        console.log('projectMethodEntityList updated')
      }
    },
  },
}
</script>

<style scoped>
.project-discription {
  text-align: right;
}

.header {
  width: 70%;
  margin: 0 auto;
  justify-content: center;
}

.app-container {
  font-family: Arial, sans-serif;
}

.top-bar {
  margin-left: 265px;
  display: flex;
  justify-content: space-between;
  padding: 10px;
}

.generated-info {
  text-align: right;
}

.content-container {
  display: flex;
  justify-content: center;
  position: relative;
  /* Ensure child absolute positioning works */
}

.filter {
  margin-bottom: 10px;
}

.entity-list {
  border: black 2px solid;
  list-style: none;
  padding: 5px;
  flex-grow: 1;
  /* Allows list to grow and take space */
}

.entity-list li {
  margin: 5px 0;
}

/* Specific Information of the Entity */
.entity-info {
  height: 100px;
  border: 2px solid black;
  padding: 10px;
  margin-top: 20px;
  background-color: #f1f1f1;
}

.main-content {
  border: black 2px solid;
  padding: 20px;
  width: 800px;
}

.statistics-header {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-box {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.graph-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.graph-box {
  text-align: center;
}

.bottom {
  display: flex;
  justify-content: center;
}

.check-button {
  margin-right: 10px;
  padding: 3px 10px;
  background-color: white;
  color: #2196f3;
  border: #2196f3 solid;
  border-width: 2px;
  border-radius: 5px;
  cursor: pointer;
}

.left-header {
  width: 50%;
  display: inline-block;
  height: 40px;
}

.right-header {
  width: 50%;
  display: inline-block;
  height: 40px;
}
</style>
