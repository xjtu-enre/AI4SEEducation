<template>
    <div>
        <div class="div2-top">
            <h4 class="h4-title">度量分析</h4>
            <span class="text"></span>
            <span class="number">共 3 种度量类别，共 12 个子指标。</span>
        </div>

        <div class="div2-bottom">
            <el-table :data="tableData" highlight-current-row :span-method="mergeCategoryCells">
                <el-table-column label="类别名" prop="categoryDisplay" width="100"></el-table-column>
                <el-table-column label="子指标名" prop="subMetric" width="100"></el-table-column>
                <el-table-column label="子指标描述" prop="subMetricDescription"></el-table-column>
                <el-table-column label="查看详情" align="center" width="150">
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

        <div class="div2-chart">
            <span style="color: black;">项目指标演化 —— {{ selectedCategoryName }}</span>
            <div ref="chart" style="width: 100%; height: 400px;"></div>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="metricRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ metricAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ metricAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ metricAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>


<script>
import EventBus from '@/components/eventBus';
import * as echarts from 'echarts';
import { CHAT_API_KEY } from '../../../config/runtime';
import { normalizeData } from '../normalizeData';
import { getJsonResult, resultUrls } from '../../../services/analysisResults';

export default {
    data(){
        return{
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

            metricRating: 4.3,
            metricAdvice: {
                "总体情况": "该项目的整体软件架构质量处于中上水平。解耦性良好，模块之间耦合度较低；内聚性表现一般；封装性相对较好但有下降趋势；继承与多态使用较为保守，表明项目结构较为平坦。",
                "现存问题": [
                    "1. 传播成本（PC）自v1.8.0之后有明显升高，从0.0614上升到0.0866，说明模块间依赖关系增多，耦合程度加重。",
                    "2. 解耦水平（DL）整体呈下降趋势，从高点0.9424下降到0.9337，虽仍在合理区间但需注意趋势变化。",
                    "3. 内聚性指标（CHM, CHD）整体偏低（平均CHM ≈ 0.46，CHD ≈ 0.29），说明部分模块职责可能不够单一，代码聚合度有待提高。",
                    "4. 封装性方面，方法隐藏因子（MHF）和属性隐藏因子（AHF）均出现阶段性下滑，尤其是AHF在v1.8.0降至0.7034，暴露成员增多。",
                    "5. 多态性使用保守（PF 从 0.148 降至 0.1），继承因子（MIF/AIF）极低（均 < 0.005），继承体系使用较少，代码复用不充分。"
                ],
                "改进建议": [
                    "1. 优化模块划分，减少无效或非必要的依赖关系，特别关注PC值变化大的版本迭代部分。",
                    "2. 加强模块内职责聚合，提升CHM和CHD指标，通过引入领域驱动设计（DDD）等方法实现模块职责清晰化。",
                    "3. 审查属性和方法的访问控制权限，减少对外暴露，提升MHF与AHF指标，增强封装性和安全性。",
                    "4. 考虑合理引入继承与多态机制，在不影响维护性的前提下提升复用性和可扩展性。",
                    "5. 建立代码度量监控机制，对关键版本迭代中结构性变化进行提前预警和干预。"
                ]
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: CHAT_API_KEY,

        }
    },
    async mounted(){
        this.metricAdvice = normalizeData(this.metricAdvice);

        this.tableData = EventBus.metriData;
        this.chartData = await getJsonResult(resultUrls.metricsEvolution);
        // 默认显示 coupling 类别
        this.selectedMetrics = this.categories.coupling;
        this.renderChart();
        // console.log("this.tableData")
        // console.log(this.tableData)
        // console.log("this.metrics")
        // console.log(metrics)
        // console.log("this.chartdata")
        // console.log(this.chartData)
    },
    methods:{
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
        },
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。给你的数据是最新版本的代码度量结果" +
        "数据,请你据此来给出评分、评分依据以及分析建议。这是代码度量数据的定义概念：" + this.tableData + 
        + "。 这是项目具体度量结果： " + this.chartdata + "。接下来请你对该项目给出评分，范围为0-5分，可保留一位小数。并给出评分依据以及分析建议。请按照以下json形式回复我：" +
        " { metricRating : ,metricAdvice : {总体情况：、现存问题：、改进建议} }."

        try {
          const res = await fetch('https://xiaoai.plus/v1/chat/completions', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${this.API_KEY}`
            },
            body: JSON.stringify({
              model: 'gpt-3.5-turbo',
              messages: [{ role: 'user', content: this.userInput }]
            })
          })
  
          const data = await res.json()
          this.response =
            data.choices?.[0]?.message?.content?.trim() || '出错了。'
          this.metricRating = this.response.metricRating
          this.metricAdvice = this.response.metricAdvice
        } catch (err) {
          this.response = '请求失败，请检查网络或 API 设置。'
        } finally {
          this.loading = false
        }
      },
    },
}
</script>

<style scoped>
.div2-top {
    width: 100%;
    display: flex;
    flex-direction: column;
    /* height: 200px; */
    /* margin-bottom: 5%; */
}

.div2-top span{
    display: flex;
    text-align: left;
    margin-bottom: 1%;
}

.div2-top .h4-title{
    display: flex;
}

.div2-bottom {
    width: 100%;
    display: flex;
    flex-direction: column;
    /* height: 450px; */
    margin-bottom: 5%;
}
</style>
