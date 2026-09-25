<template>
    <div>
        <div class="div2-top">
            <h4 class="h4-title">耦合面分析</h4>
            <span class="text">基于实体归属信息提取下游相对上游移动操作系统的耦合面（即设计边界），能够从依赖层面揭示下游代码耦合上游的方式。</span>
            <span class="number">共 3 种实体归属方，共 {{ CFTotalSum }}个耦合面节点。</span>
        </div>

        <div class="div2-bottom">
            <el-table :data="CFList" highlight-current-row>
                <el-table-column label="ownership" prop="ownership"></el-table-column>
                <el-table-column label="含义" prop="explanation"></el-table-column>
                <el-table-column label="总数" prop="number" width="250" sortable></el-table-column>
            </el-table>
        </div>

        <div class="div2-chart">
            <span class="chart-title">耦合面下游实体ownership</span>
            <pieChart ref="CFChart" style="height: 600px; width: 100%;"/>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="CFRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ CFAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ CFAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ CFAdvice['改进建议'] }}</pre>
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
            CFList: [],
            CFTotalSum: 0,

            CFRating: 3.2,
            CFAdvice: {
                "总体情况": "该项目的耦合面总体数量较大（共5607个），其中多数为‘actively native’类型，说明系统中大量下游模块自行定义和扩展实体，独立性较高；但同时也存在较多‘intrusive native’耦合面，表明下游对上游实体进行了修改和依赖，存在一定程度的侵入性耦合。",
                "现存问题": [
                    "1. 'intrusive native' 耦合面数量高达965，占总耦合面的17.2%，说明系统内部耦合紧密，下游对上游实体改动较多，增加了演进成本和协同复杂度。",
                    "2. 'actively native' 耦合面占比超过81%（4571个），虽然增强了模块自治性，但也可能导致重复建模、语义不一致和平台能力碎片化。",
                    "3. 'extensive' 耦合面比例极低（仅71个，占比1.3%），说明共享实体的标准化和复用机制建设不足，未能形成统一的跨系统通用能力。"
                ],
                "改进建议": [
                    "1. 推动对‘intrusive native’耦合面的治理，减少下游对上游实体的修改行为，采用扩展点、适配器等方式降低侵入性耦合。",
                    "2. 梳理并统一下游系统中大量‘actively native’定义的实体，提升平台统一建模水平，避免冗余与语义冲突。",
                    "3. 强化共享实体的标准化建设，推动更多‘extensive’类型的耦合面，以提高系统模块之间的一致性与协同效率。",
                    "4. 引入耦合面评审机制，在设计阶段评估耦合面类型及其合理性，避免无意识地引入不良耦合方式。",
                    "5. 在技术平台层提供能力注册和复用机制，降低下游系统重复建模的必要性，提升整体架构的可控性与可维护性。"
                ]
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: api_key.API_KEY,

        }
    },
    mounted(){
        this.CFAdvice = normalizeData(this.CFAdvice);

        this.CFList = EventBus.CFList
        this.CFTotalSum = this.CFList.reduce((accumulator, currentObject) => {
            return accumulator + currentObject.number;
        }, 0);
        this.$refs.CFChart.initPieChart({
            data: this.CFList.map((item) => ({
              name: item.ownership,
              value: item.number,
            })),
        })
        // console.log("this.CFTotalSum")
        // console.log(this.CFTotalSum)
        // console.log("this.CFList")
        // console.log(this.CFList)

    },
    methods:{
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。给你的数据是最新版本的耦合面分析" +
        "数据,请你据此来给出评分、评分依据以及分析建议。这是耦合面的总数：" + this.CFTotalSum + 
        + "。 这是耦合面的列表： " + this.CFList + "。接下来请你对该项目给出评分，范围为0-5分，可保留一位小数。并给出评分依据以及分析建议。请按照以下json形式回复我：" +
        " { CFRating : ,CFAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.CFRating = this.response.CFRating
          this.CFAdvice = this.response.CFAdvice
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