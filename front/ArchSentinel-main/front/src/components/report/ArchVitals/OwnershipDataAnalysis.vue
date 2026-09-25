<template>
    <div>
        <div class="div2-top">
            <h4 class="h4-title">实体归属</h4>
            <span class="text">基于上下游代码的依赖图，构建上下游实体间实体映射关系；基于实体映射关系与实体文本信息，检测下游系统实体相对于上游实体变更类型，并提取下游实体归属方信息。</span>
            <span class="number">共 3 种实体归属方，共 824562 条实体归属信息。</span>
        </div>

        <!-- <div class="div2-bottom">
            <el-table :data="OSList" highlight-current-row>
                <el-table-column label="ID" prop="ID" width="100"></el-table-column>
                <el-table-column label="ownership" prop="ownership" width="180"></el-table-column>
                <el-table-column label="category" prop="category" width="130"></el-table-column>
                <el-table-column label="qualifiedName" prop="qualifiedName"></el-table-column>
            </el-table>
        </div> -->

        <div class="div2-chart">
            <span class="chart-title">实体归属统计</span>
            <pieChart ref="OSChart" style="height: 600px; width: 100%;"/>
            <span class="chart-title">实体类别统计</span>
            <pieChart ref="OSTypeChart" style="height: 600px; width: 100%;"/>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="OSRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ OSAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ OSAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ OSAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import EventBus from '@/components/eventBus';
import pieChart from '../pieChart.vue';
import api_key from "../../../../public/api_key.json";
import { normalizeData } from '../normalizeData';

export default {
    components: {pieChart},
    data(){
        return{
            OSList: [],
            ownershipTypeCounts: {},
            categoryTypeCounts: {},

            OSRating: 4.6,
            OSAdvice: {
                "总体情况": "项目实体总数极为庞大，达80余万个，绝大多数（约99%）实体为下游系统独立定义的 'actively native' 类型，表明系统模块之间存在高度自治；同时也检测到少量 'intrusive native'（3401个）和 'extensive'（4802个）实体，显示存在一定程度的上下游依赖与复用。",
                "现存问题": [
                    "1. 实体归属严重碎片化，actively native 实体数量高达816359个，占比超99%，说明系统缺乏有效的复用和统一建模策略，导致大量冗余定义和平台能力分裂。",
                    "2. intrusive native 实体虽占比较小（0.4%），但绝对数量（3401个）仍较高，说明部分下游模块对上游实体有侵入性变更行为，存在架构一致性风险。",
                    "3. extensive 实体数量偏低，仅4802个，表明共享建模和复用机制不健全，跨模块通用能力建设不足。",
                    "4. 实体类别分布不均，Variable 和 Method 占据压倒性比例（共计近79%），可能存在代码膨胀、命名混乱、逻辑集中度高等问题，影响可维护性。"
                ],
                "改进建议": [
                    "1. 加强平台统一建模机制，推动从 'actively native' 转向更多 'extensive' 类型的实体复用，提升系统一致性与协同效率。",
                    "2. 规范下游系统扩展行为，限制或重构 intrusive native 实体，避免对上游模块的侵入性修改。",
                    "3. 建立实体定义注册与审核机制，避免重复建模、无效冗余定义，引导开发者优先复用平台已有能力。",
                    "4. 推动基于语义的实体对齐与聚合分析，识别同义实体并归一化处理，减少语义漂移风险。",
                    "5. 对 Variable 和 Method 等高频实体，结合代码质量指标开展复杂度治理与模块解耦工作，提升结构清晰度与可维护性。"
                ]
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: api_key.API_KEY,
        }
    },
    mounted(){
        this.OSAdvice = normalizeData(this.OSAdvice);
        
        this.OSList = EventBus.OSList;
        this.ownershipTypeCounts = EventBus.ownershipTypeCounts
        this.categoryTypeCounts = EventBus.categoryTypeCounts
        console.log('ow分析', this.ownershipTypeCounts, this.categoryTypeCounts)

        this.$refs.OSChart.initPieChart({
            data: Object.keys(this.ownershipTypeCounts).map((type) => ({
                value: this.ownershipTypeCounts[type],
                name: type,
            })),
        })

        this.$refs.OSTypeChart.initPieChart({
            data: Object.keys(this.categoryTypeCounts).map((type) => ({
                value: this.categoryTypeCounts[type],
                name: type,
            })),
        })
    },
    methods:{
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。给你的数据是最新版本的实体归属信息分析" +
        "数据,基于上下游代码的依赖图，构建上下游实体间实体映射关系；基于实体映射关系与实体文本信息，检测下游系统实体相对于上游实体变更类型，并提取下游实体归属方信息。" +
        "请你据此来给出评分、评分依据以及分析建议。这是检测到的实体归属统计：" + this.ownershipTypeCounts + 
        + "。 这是检测到的实体类别统计： " + this.categoryTypeCounts + "。接下来请你对该项目给出评分，范围为0-5分，可保留一位小数。并给出评分依据以及分析建议。请按照以下json形式回复我：" +
        " { OSRating : ,OSAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.OSRating = this.response.OSRating
          this.OSAdvice = this.response.OSAdvice
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