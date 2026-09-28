<template>
  <div>
    <div class="content-container">
      <div class="statistics-header">
        <div class="stat-box">
          <div class="stat-value" v-if="unref(projectLines)">
            {{ projectLines.records[0].lineNumber }}
          </div>
          <div class="stat-value" v-else>Loading...</div>
          <div class="stat-label">Lines</div>
        </div>
        <div class="stat-box">
          <div class="stat-value" v-if="unref(projectProperty)">
            {{ projectProperty.fileEntityNumber }}
          </div>
          <div class="stat-value" v-else>Loading...</div>
          <div class="stat-label">Files</div>
        </div>
        <div class="stat-box">
          <div class="stat-value" v-if="unref(projectProperty)">
            {{ projectProperty.classEntityNumber }}
          </div>
          <div class="stat-value" v-else>Loading...</div>
          <div class="stat-label">Classes</div>
        </div>
        <div class="stat-box">
          <div class="stat-value" v-if="unref(projectProperty)">
            {{ projectProperty.methodEntityNumber }}
          </div>
          <div class="stat-value" v-else>Loading...</div>
          <div class="stat-label">Methods</div>
        </div>
      </div>
    </div>

    <div class="content-container">
      <div class="graph-section">
        <div class="graph-box">
          <!-- <div style="background-color: #f1f1f1; width: 185px; height: 185px;">
                    <sunburst-chart width="180px" height="180px" />
                </div>
                <div>Directory Structure</div> -->
          <div v-if="unref(projectLines)" style="width: 280px; height: 250px">
            <doughnut-chart
              :id-number="1"
              :width="'280px'"
              :height="'250px'"
              :chart-data="[
                { value: projectLines.records[0].codeLines, name: 'Code' },
                {
                  value: projectLines.records[0].commentLines,
                  name: 'Comment',
                },
                { value: projectLines.records[0].blankLines, name: 'Blank' },
              ]"
            ></doughnut-chart>
          </div>
          <div v-else style="width: 280px; height: 250px">
            <h3>Loading...</h3>
          </div>

          <div>Line Breakdown</div>
        </div>
        <div class="graph-box">
          <!-- <img src="largest-functions.png" alt="Largest Functions" /> -->
          <div
            v-if="unref(topLargestMethods)"
            style="width: 280px; height: 250px"
          >
            <bar-chart
              :id-number="1"
              :width="'280px'"
              :height="'250px'"
              :chart-data="{
                xAxisItem: topLargestMethods.map((item) => item.entityName),
                data: topLargestMethods.map(
                  (item) => item.endLine - item.startLine + 1
                ),
              }"
            ></bar-chart>
          </div>
          <div v-else style="width: 280px; height: 250px">
            <h3>Loading...</h3>
          </div>

          <div>Largest Functions</div>
        </div>
        <div class="graph-box">
          <!-- <img src="largest-classes.png" alt="Largest Classes" /> -->
          <div
            v-if="unref(topLargestClasses)"
            style="width: 280px; height: 250px"
          >
            <bar-chart
              :id-number="2"
              :width="'280px'"
              :height="'250px'"
              :chart-data="{
                xAxisItem: topLargestClasses.map((item) => item.entityName),
                data: topLargestClasses.map(
                  (item) => item.endLine - item.startLine + 1
                ),
              }"
            ></bar-chart>
          </div>
          <div v-else style="width: 280px; height: 250px">
            <h3>Loading...</h3>
          </div>

          <div>Largest Classes</div>
        </div>
        <div class="graph-box">
          <!-- <img src="most-complex-classes.png" alt="Most Complex Classes" /> -->
          <div
            v-if="unref(topLargestClasses)"
            style="width: 280px; height: 250px"
          >
            <bar-chart
              :id-number="3"
              :width="'280px'"
              :height="'250px'"
              :chart-data="{
                xAxisItem: topLargestClasses.map((item) => item.entityName),
                data: topLargestClasses.map(
                  (item) => item.endLine - item.startLine + 1
                ),
              }"
            ></bar-chart>
          </div>
          <div v-else style="width: 280px; height: 250px">
            <h3>Loading...</h3>
          </div>

          <div>Most Complex Classes</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { unref } from 'vue'
import {
  useQueryByScanUuid,
  useQueryEntitiesByCategory,
  useQueryLineByScanUuid,
} from './script/architecture'
import DoughnutChart from './charts/DoughnutChart.vue'
import BarChart from './charts/BarChart.vue'
import SunburstChart from './charts/SunburstChart.vue'

export default {
  props: ['scanUuid'], // Vue 2 需要用 props 选项，而不是 defineProps()

  components: {
    DoughnutChart,
    BarChart,
    SunburstChart,
  },

  data() {
    return {
      projectProperty: null,
      projectLines: null,
      projectMethodEntityList: null,
      projectClassEntityList: null,
      topLargestMethods: null,
      topLargestClasses: null,

      // Vue 3 Composition API 查询改为 Vue 2 的方式
      propertyResult: null,
      lineResult: null,
      methodEntityResult: null,
      classEntityResult: null,
    }
  },

  created() {
    // 初始化数据（Vue 2 里不能直接使用 `ref()`，但我们可以在 `setup` 里用）
    this.propertyResult = useQueryByScanUuid(this.scanUuid)
    this.lineResult = useQueryLineByScanUuid(this.scanUuid, 1, 1)
    this.methodEntityResult = useQueryEntitiesByCategory(
      this.scanUuid,
      'Method'
    )
    this.classEntityResult = useQueryEntitiesByCategory(this.scanUuid, 'Class')

    // 监听数据变化
    this.$watch(
      () => this.lineResult.data,
      (newValue) => {
        if (newValue?.value) {
          this.projectLines = newValue.value
          console.log('projectLines updated')
        }
      },
      { deep: true }
    )

    this.$watch(
      () => this.propertyResult.data,
      (newValue) => {
        if (newValue?.value) {
          this.projectProperty = newValue.value
          console.log('projectProperty updated')
        }
      },
      { deep: true }
    )

    this.$watch(
      () => this.methodEntityResult.data,
      (newValue) => {
        if (newValue?.value) {
          this.projectMethodEntityList = newValue.value
          this.topLargestMethods = this.getTopNLargestEntities(
            this.projectMethodEntityList,
            4
          )
          console.log('projectMethodEntityList updated')
        }
      },
      { deep: true }
    )

    this.$watch(
      () => this.classEntityResult.data,
      (newValue) => {
        if (newValue?.value) {
          this.projectClassEntityList = newValue.value
          this.topLargestClasses = this.getTopNLargestEntities(
            this.projectClassEntityList,
            4
          )
          console.log('projectClassEntityList updated')
        }
      },
      { deep: true }
    )

    console.log('lineResult.data =', this.lineResult.data)
    console.log('projectLines =', this.projectLines)
  },

  methods: {
    unref,
    getTopNLargestEntities(entityList, n) {
      const topEntities = unref(entityList)
        .sort((a, b) => b.endLine - b.startLine - (a.endLine - a.startLine))
        .slice(0, n)
      console.log(`top ${n} largest entities: `, topEntities)
      return topEntities
    },
  },
}
</script>

<style scoped>
.content-container {
  display: flex;
  justify-content: center;
}

.statistics-header {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-box {
  text-align: center;
  margin: 0px 20px;
  width: 280px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 24px;
  color: #666;
}

.graph-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.graph-box {
  text-align: center;
  margin: 0px 20px;
}
</style>
