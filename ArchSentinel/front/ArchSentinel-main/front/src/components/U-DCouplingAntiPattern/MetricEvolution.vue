<template>
  <div class="MetricEvolution">
    <div class="main">
      <!-- 表格部分 -->
      <div class="top">
        <div class="right-table">
          <el-table
            class="right-table-in"
            :data="tableData"
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
            :span-method="mergeCategoryCells"
          >
            <el-table-column label="类别名" prop="categoryDisplay" width="150"></el-table-column>
            <el-table-column label="子指标名" prop="subMetric"></el-table-column>
            <el-table-column label="子指标描述" prop="subMetricDescription"></el-table-column>
            <el-table-column label="查看详情" align="center" >
              <template slot-scope="scope">
                <template v-if="isFirstRowOfCategory(scope.row, scope.$index)">
                  <el-button
                    size="mini"
                    type="success"
                    @click="viewCategoryChart(scope.row.category)"
                    style="text-decoration: underline; color: white; background-color: #4CAF50; border-color: #4CAF50; display: block; margin: 0 auto; padding: 10px 20px; vertical-align: middle;"
                  >
                    查看折线图
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 折线图部分 -->
      <div class="bottom">
        <h2 style="color: black;">项目指标演化 —— {{ selectedCategoryName }}</h2>
        <div ref="chart" style="width: 100%; height: 500px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { resultUrls } from '../../services/analysisResults';
import * as echarts from 'echarts';
import EventBus from '../eventBus';

export default {
  data() {
    return {
      tableData: [],
      chartData: null,
      selectedMetrics: [],
      categories: {
        coupling: ['PC', 'DL', 'ODD', 'IDD', 'CF'],
        cohesion: ['CHM', 'CHD'],
        inheritance: ['MIF', 'AIF', 'PF'],
        encapsulation: ['MHF', 'AHF']
      },
      categoryNameMap: {
      coupling: '耦合',
      cohesion: '内聚',
      inheritance: '继承',
      encapsulation: '封装'
      },
      selectedCategoryName: '耦合',

    };
  },
  async mounted() {
    const [descriptionResponse, metricsResponse] = await Promise.all([
      axios.get(resultUrls.metricsDescription),
      axios.get(resultUrls.metricsEvolution)
    ]);
    const jsonData = descriptionResponse.data;
    const metrics = metricsResponse.data;
    this.tableData = this.formatTableData(jsonData);
    EventBus.metriData = this.tableData;
    this.chartData = metrics;
    // 默认显示 coupling 类别
    this.selectedMetrics = this.categories.coupling;
    this.renderChart();
  },
  methods: {
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;';
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;';
    },
    formatTableData(descriptionArray) {
      // 把 array 转为 Map: name => explanation
      const descMap = new Map();
      descriptionArray.forEach(item => {
        descMap.set(item.name, item.explanation);
      });

      // 构造最终用于表格的数据
      const output = [];
      Object.entries(this.categories).forEach(([category, metrics]) => {
        metrics.forEach(subMetric => {
          output.push({
            category,
            categoryDisplay: this.categoryNameMap[category], // 新增字段
            subMetric,
            subMetricDescription: descMap.get(subMetric) || '无描述'
          });
        });
      });

      return output;
    },
    mergeCategoryCells({ row, column, rowIndex }) {
      if (column.property === 'categoryDisplay') {
        const currentCategory = row.categoryDisplay;
        const firstIndex = this.tableData.findIndex(r => r.categoryDisplay === currentCategory);
        const rowSpan = this.tableData.filter(r => r.categoryDisplay === currentCategory).length;
        if (rowIndex === firstIndex) {
          return {
            rowspan: rowSpan,
            colspan: 1
          };
        } else {
          return {
            rowspan: 0,
            colspan: 0
          };
        }
      }
      // 处理 查看详情 列的合并
    if (column.label === '查看详情') {
      const currentCategory = row.categoryDisplay;
      const firstIndex = this.tableData.findIndex(r => r.categoryDisplay === currentCategory);
      const rowSpan = this.tableData.filter(r => r.categoryDisplay === currentCategory).length;
      if (rowIndex === firstIndex) {
        return {
          rowspan: rowSpan,
          colspan: 1
        };
      } else {
        return {
          rowspan: 0,
          colspan: 0
        };
      }
    }
    },
    // mergeDetailButton({ row, column, rowIndex }) {
    //   const currentCategory = row.category;
    //   const firstIndex = this.tableData.findIndex(r => r.category === currentCategory);
      
    //   // 计算与该类别相关的子指标数量
    //   const rowSpan = this.tableData.filter(r => r.category === currentCategory).length;
      
    //   // 在第一行的情况下，跨越所有子指标列
    //   if (rowIndex === firstIndex) {
    //     return {
    //       rowspan: rowSpan,
    //       colspan: 3  // 按钮跨越 "子指标名"、"子指标描述" 和 "查看详情" 三列
    //     };
    //   } else {
    //     return {
    //       rowspan: 0,
    //       colspan: 0
    //     };
    //   }
    // },
    isFirstRowOfCategory(row, index) {
      const category = row.category;
      const firstIndex = this.tableData.findIndex(r => r.category === category);
      return index === firstIndex;
    },
    viewCategoryChart(category) {
      this.selectedMetrics = this.categories[category];
      this.selectedCategoryName = this.categoryNameMap[category];
      this.renderChart();
    },
    renderChart() {
      if (!this.chartData || !this.selectedMetrics.length) return;

      const chart = echarts.init(this.$refs.chart);
      chart.clear();
      const versions = this.chartData.versions;

      const seriesData = this.selectedMetrics.map(metricName => ({
        name: metricName,
        type: 'line',
        data: this.chartData.metrics[metricName] || []
      }));

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          top: 30,
          data: this.selectedMetrics
        },
        xAxis: {
          type: 'category',
          data: versions,
          name: 'Version'
        },
        yAxis: {
          type: 'value',
          name: 'Metric Value'
        },
        series: seriesData
      };

      chart.setOption(option);
    }
  }
};
</script>

<style scoped>
.MetricEvolution {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
  display: flex;
  flex-direction: column;
}

.main {
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.top {
  width: 100%;
  height: 40%;
  margin-bottom: 20px;
}

.bottom {
  width: 100%;
  height: 50%;
}

.right-table {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
}

.right-table-in {
  width: 90%;
  margin: 0 auto;
}
</style>
