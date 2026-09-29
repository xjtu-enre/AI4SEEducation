<template>
    <section class="report-section">
        <h2>项目分析</h2>
        <div class="section-div">
            <h3>项目基础信息</h3>
            <div class="section-div-div">
                <div class="div2-top">
                    <div class="div2-left">
                        <!-- <span><img src="@/assets/img/report/语言.png" alt="pmd" class="icon">下游项目语言分布</span> -->
                        <span>下游项目语言分布</span>

                        <pieChart ref="languagePieChart" style="height: 100%; width: 100%;"/>
                    </div>
                    <div class="div2-right" style="display: flex; align-items: center; height: 100%;">
                        <el-table
                            :data="basicInfoList"
                            :header-cell-style="getHeaderStyle"
                        >
                            <el-table-column prop="project" width="100"></el-table-column>
                            <el-table-column prop="projectName" label="项目名" ></el-table-column>
                            <el-table-column prop="projectSize" label="项目大小" ></el-table-column>
                            <el-table-column prop="fileCount" label="文件数量" ></el-table-column>
                            <el-table-column prop="cloc" label="代码行数" ></el-table-column>

                        </el-table>
                    </div>
                </div>
                <div class="div2-bottom">
                    <div class="div2-left">
                        <span>{{ UpFileInfo.folderName }} vs {{ DownFileInfo.folderName }} 项目实体类型分布对比</span>
                        <barChart ref="entityBarChart" style="height: 100%; width: 100%;"/>
                    </div>
                    <div class="div2-right">
                        <span>{{ UpFileInfo.folderName }} vs {{ DownFileInfo.folderName }} 项目关系类型分布对比</span>
                        <barChart ref="relationBarChart" style="height: 100%; width: 100%;"/>
                    </div>
                </div>
                <div style="height: 600px;width: 100%;display: flex;flex-direction: column;">
                    <div style="height: 200px;margin-top: 20px;">
                        <span style="color: black;font-size: 1.1rem;">项目级指标雷达图</span>
                        <div class="category-button-group">
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
                    </div>
                    <div class="div2-bottom" style="height: 500px">
                        <div class="div2-left">
                            <span>上游项目</span>
                            <div ref="upFilechart" style="height: 400px; width: 100%;"></div>
                        </div>
                        <div class="div2-right">
                            <span>下游项目</span>
                            <div ref="downFilechart" style="height: 400px; width: 100%;"></div>
                        </div>
                    </div>
                    <div style="height: 50px;display: flex">
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
                </div>
                <div class="div2-advice">
                    <div class="div2-advice-rate">
                        <span>评分：</span>
                        <el-rate v-model="basicRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
                    </div>
                    <div class="div2-advice-text">
                        <span style="text-align: left;">建议：</span>
                        <div>
                          <h4 style="text-align: left;">总体情况：</h4>
                          <pre style="white-space: pre-wrap; text-align: left;">{{ basicAdvice['总体情况'] }}</pre>
                        </div>
                        <div>
                          <h4 style="text-align: left;">现存问题：</h4>
                          <pre style="white-space: pre-wrap; text-align: left;">{{ basicAdvice['现存问题'] }}</pre>
                        </div>
                        <div>
                          <h4 style="text-align: left;">改进建议：</h4>
                          <pre style="white-space: pre-wrap; text-align: left;">{{ basicAdvice['改进建议'] }}</pre>
                        </div>
                    </div>
                      
                </div>
            </div>
        </div>
        <div class="section-div">
            <h3>ArchInspector 结果分析</h3>
            <ArchInspectorAnalysis />
        </div>
        <div class="section-div">
            <h3>ArchCompass 结果分析</h3>
            <ArchCompassAnalysis />
        </div>
        <div class="section-div">
            <h3>ArchVitals 结果分析</h3>
            <ArchVitalsAnalysis />
        </div>
      </section>
</template>

<script>
import EventBus from '../eventBus'
import barChart from './upDonwBarChart.vue'
import pieChart from './pieChart.vue'
import ArchInspectorAnalysis from './ArchInspectorAnalysis.vue';
import ArchCompassAnalysis from './ArchCompassAnalysis.vue';
import ArchVitalsAnalysis from './ArchVitalsAnalysis.vue';
import { CHAT_API_KEY } from '../../config/runtime'
import * as echarts from 'echarts';
import { getJsonResult, resultUrls } from '../../services/analysisResults';
import { normalizeData } from './normalizeData';

export default {
    components: { pieChart, barChart, ArchInspectorAnalysis, ArchCompassAnalysis, ArchVitalsAnalysis },
    data(){
        return {
            UpFileInfo: {
                folderName: '未上传上游项目',
                literallySize: 0,
                fileCount: 0,
                cloc: 0
            },
            DownFileInfo: {
                folderName: '未上传下游项目',
                literallySize: 0,
                fileCount: 0,
                cloc: 0
            },

            currentCategory: 'coupling',
            categories: {
                coupling: ['PC', 'DL', 'ODD', 'IDD', 'CF'],
                cohesion: ['CHM', 'CHD'],
                inheritance: ['MIF', 'AIF', 'PF'],
                encapsulation: ['MHF', 'AHF']
            },
            upFilechart: null,
            downFilechart: null,
            upMetricData: {},
            downMetricData: {},

            basicInfoList: [],
            basicRating: 3.5,
            basicAdvice: {
                "总体情况": "项目从原始版本（avro-1.7.3）到最终版本（avro-1.12.0）呈现出显著增长：项目体积从8.22MB增加到13.8MB，代码行数从154,411增长到249,992，几乎翻倍。实体和关系的类型数量也几乎成倍增长，表明系统功能、模块和复杂度都有显著扩展。这说明项目持续演进、功能增强、模块增多，代码结构也趋于更复杂、规模化。",
                "现存问题": "1. 项目规模大幅增长但未见明显模块化拆分迹象，文件数量增幅（约2倍）与代码行数增幅不成正比，可能导致单文件/单模块代码过大，增加维护难度。\n2. 变量和方法数量的高速增长（变量增长约2倍、方法增长近1倍）暗示代码逻辑变得更加密集，可能存在类/方法职责过多的问题。\n3. 关系类型如Call、Set、Parameter、UseVar的倍数增长说明模块之间耦合性增强，需要警惕潜在的高耦合风险。\n4. 多态、继承、反射（Override、Inherit、Reflect）等关系数量也显著增长，可能对系统运行时行为理解与测试带来挑战。",
                "改进建议": "1. 加强模块解耦和职责划分，采用包拆分、领域驱动设计或微服务划分策略，缓解模块膨胀问题。\n2. 对新增类和方法进行职责审查，防止出现‘上帝类’、‘长方法’等坏味道。\n3. 倡导接口优先、组合优于继承、依赖注入等设计原则，减少继承链和运行时动态行为的复杂性。\n4. 引入静态代码分析工具（如SonarQube）结合架构图谱，持续监控实体、依赖、调用等演进趋势，防止技术债积累。\n5. 建议将大型模块重构为更小、边界清晰的子系统，并逐步引入自动化测试和接口契约验证，以降低因规模扩大带来的回归风险。"
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: CHAT_API_KEY
        }
    },
    async mounted() {
        [this.upMetricData, this.downMetricData] = await Promise.all([
            getJsonResult(resultUrls.metricsPre),
            getJsonResult(resultUrls.metricsNext)
        ])
        this.basicAdvice = normalizeData(this.basicAdvice)
        
        this.UpFileInfo = EventBus.UpFileInfo || this.UpFileInfo
        this.DownFileInfo = EventBus.DownFileInfo || this.DownFileInfo
        this.getBasicInfo()

        this.$refs.languagePieChart.initPieChart({ data: EventBus.languageData || [] });

        this.$refs.entityBarChart.initBarChart({
            title: '项目实体类型分布对比',
            upData: EventBus.upData.entity,
            downData: EventBus.downData.entity,
        });
        this.$refs.relationBarChart.initBarChart({
            upData: EventBus.upData.relation,
            downData: EventBus.downData.relation
        });
        // console.log('this.basicInfoList')
        // console.log(this.basicInfoList)
        // console.log('项目实体类型分布对比')
        // console.log(EventBus.upData.entity)
        // console.log(EventBus.downData.entity)
        // console.log('项目关系类型分布对比')
        // console.log(EventBus.upData.relation)
        // console.log(EventBus.downData.relation)
        this.upFilechart = echarts.init(this.$refs.upFilechart);
        this.downFilechart = echarts.init(this.$refs.downFilechart);
        this.updateChart(this.upMetricData, this.upFilechart);
        this.updateChart(this.downMetricData, this.downFilechart);
    },
    methods:{
        getBasicInfo(){
            this.basicInfoList = [
                {project:'上游项目', projectName: this.UpFileInfo.folderName, projectSize: this.UpFileInfo.literallySize, fileCount: this.UpFileInfo.fileCount, cloc: this.UpFileInfo.cloc},
                {project:'下游项目', projectName: this.DownFileInfo.folderName, projectSize: this.DownFileInfo.literallySize, fileCount: this.DownFileInfo.fileCount, cloc: this.DownFileInfo.cloc}
            ]
        },
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是代码审查工程师，现在需要让你来根据代码的扫描结果来进行评分以及建议。给你的数据分别是代码的原始版本和多次更迭后的版本：" + 
        "我会给你项目的原始版本和迭代版本的项目大小对比、实体类型和关系类型的数目对比，请你据此来给出评分以及建议。" + 
        "首先是项目的原始版本和迭代版本的项目大小对比：原始版本和迭代版本的的项目信息：" + this.basicInfoList + "。" +
        "接下来是项目的实体类型和关系类型的数目对比：" + "首先是项目实体类型分布对比：" + "原始版本：" + EventBus.upData.entity + "。最终版本：" + EventBus.downData.entity + 
        "接着是项目关系类型分布对比：" + "原始版本：" + EventBus.upData.relation + "。最终版本：" +  EventBus.downData.relation + 
        "。接下来请你对该项目从原始版本到最终版本的项目大小、实体类型和关系类型的数目变化来给出评分，范围为0-5分。并给出项目更迭的建议。要求建议分为：1.总体情况、2.现存问题、3.改进建议。请按照以下json形式回复我：" + 
        " { basicRating : , basicAdvice : {总体情况：、现存问题：、改进建议}}."
  
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
          this.basicRating = this.response.basicRating
          this.basicAdvice = this.response.basicAdvice
        } catch (err) {
          this.response = '请求失败，请检查网络或 API 设置。'
        } finally {
          this.loading = false
        }
      },
        getHeaderStyle() {
            return 'background-color:transparent; color: #000000; text-align: left; padding: 0;'
        },
        switchCategory(cat) {
            this.currentCategory = cat;
            this.updateChart(this.upMetricData, this.upFilechart);
            this.updateChart(this.downMetricData, this.downFilechart);
        },
        updateChart(metricData, chart) {
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

            chart.setOption(option);
        },
    }
}
</script>

<style scoped>
.report-section {
  margin-bottom: 30px;
  padding: 24px;
  border: 1px solid #e6e6e6;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.report-section h2 {
  margin-bottom: 20px;
  color: #1a1a1a;
  font-weight: 600;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.section-div {
  margin-bottom: 20px;
  padding: 24px;
  border: 1px solid #e6e6e6;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-div h3 {
  color: black;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.section-div-div{
    display: flex;
    flex-direction: column;
    width: 100%;
}

.div2-top {
    width: 100%;
    display: flex;
    height: 300px;
    margin-bottom: 5%;
}

.div2-left {
    width: 50%;
}

.div2-left span{
    color: black;
    font-size: 1.1rem;
}

.div2-right {
    width: 50%;
}

.div2-right span{
    color: black;
    font-size: 1.1rem;
}

.div2-bottom {
    width: 100%;
    display: flex;
    height: 450px;
}

::v-deep.el-rate .el-rate__icon {
  font-size: 30px;
}

.icon {
  /* vertical-align: middle; 垂直居中对齐 */
  margin-right: 8px; /* 与文本的间距 */
  width: 20px; /* 图标宽度 */
  height: 20px; /* 图标高度 */
}

.category-button-group {
  display: grid;
  /* grid-template-columns: repeat(2, 1fr); 每行两个按钮 */
  grid-template-columns: repeat(4, 1fr);
  gap: 16px; /* 行列间距 */
  justify-items: center; /* 居中每个按钮 */
  margin: 20px 0 20px;
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
  margin: 10px 0 0 10px;
  color: #333;
  width: 100%;
  text-align: left;
  font-size: 1.1rem;
}
.category-description p {
  margin: 5px 0;
  line-height: 1.6;
}
</style>
