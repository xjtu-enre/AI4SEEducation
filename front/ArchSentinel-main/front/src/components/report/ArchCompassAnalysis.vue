<template>
    <div class="section-div-div">
        <div class="div2-top">
            <h4 class="h4-title">通用反模式检测</h4>
            <span class="text">反模式是软件系统中的一种潜在征兆，意味着软件将会出现质量下降甚至失效的情况</span>
            <span class="number">共 7 种通用反模式，共 {{ antiPatternTotalSum }} 条违规</span>
        </div>

        <div class="div2-bottom">
            <el-table
                :data="antiPatternList"
                highlight-current-row
            >
                <el-table-column prop="name" label="名称" width="100" />
                <el-table-column prop="fullName" label="全称" width="260"/>
                <el-table-column prop="chineseName" label="中文" width="150" />
                <el-table-column prop="content" label="解释" />
                <el-table-column prop="total" label="总数" width="100" sortable/>
            </el-table>
        </div>

        <div class="div2-chart">
            <span class="chart-title">通用反模式数量占比</span>
            <pieChart ref="commonAntiPieChart" style="height: 600px; width: 100%;"/>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="antiPatternRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ antiPatternAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ antiPatternAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ antiPatternAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>

        <div class="div2-top" style="margin-top: 5%;">
            <h4 class="h4-title">自定义反模式检测</h4>
            <span class="text">用户可以自己用特定语言或自然语言描述的架构质量与安全隐患症状</span>
            <span class="number">共 4 种自定义反模式，共 {{ antiPatternTotalSum }} 条违规</span>
        </div>

        <div class="div2-bottom">
            <el-table
                :data="ViolationInfoList"
                highlight-current-row
            >
                <el-table-column label="rule" prop="rule"></el-table-column>
                <el-table-column label="优先级" prop="content" width="100"></el-table-column>
                <el-table-column label="数目" prop="total" sortable width="100"></el-table-column>
            </el-table>
        </div>

        <div class="div2-chart">
            <span class="chart-title">自定义反模式数量占比</span>
            <pieChart ref="ViolationInfoPieChart" style="height: 600px; width: 100%;"/>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="ViolationInfoRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ViolationInfoAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ViolationInfoAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ViolationInfoAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import EventBus from '../eventBus';
import pieChart from './pieChart.vue';
import barChart from './barChart.vue';
import api_key from "../../../public/api_key.json";
import { normalizeData } from './normalizeData';


export default {
    components: {pieChart, barChart},
    data(){
        return {
            antiPatternList: [],
            antiPatternTotalSum: 0,

            antiPatternRating: 3.8,
            antiPatternAdvice: {
                "总体情况": "当前项目在架构通用反模式方面存在较多严重违规，尤其是“接口未解耦（AWD）”、“特性依恋（FE）”和“霰弹式修改（SS）”的违规数量非常高，反映出架构的耦合度高、内聚性差、变更扩展困难等问题。",
                "现存问题": "1. 接口未解耦（AWD）高达4074次，说明抽象未有效隔离实现，接口设计不合理；2. 特性依恋（FE）610次，表示方法设计不符合职责单一原则；3. 霰弹式修改（SS）552次，意味着系统可维护性低；4. 多重路径继承（MH）和循环继承（CH）等继承层次问题也存在，可能导致继承体系混乱。",
                "改进建议": "1. 优化抽象层与实现层的解耦，避免同一类同时依赖接口和实现；2. 重构方法过度访问其他类数据的问题，推动高内聚设计；3. 清理继承结构，合并冗余路径，防止循环继承；4. 针对频繁变动模块实施模块化改造，降低修改扩散风险。"
            },
            ViolationInfoList: [],
            ViolationInfoListTotalSum: 0,
            ViolationInfoRating: 3.7,
            ViolationInfoAdvice: {
                "总体情况": "自定义架构反模式的问题相对通用反模式略好，但仍存在一些中高风险的继承结构和类修饰符使用问题，说明在架构策略规范性上还有待加强。",
                "现存问题": "1. 高优先级问题如 activelyNative 类被 extensive 或 private 类继承，违规131次，违反封闭/开放原则；2. public 类被 private 类继承（173次）影响可见性管理；3. STATIC 类使用不当（65次、250次）表明静态类职责与结构设计不清晰。",
                "改进建议": "1. 明确类之间的继承策略与访问控制规则，避免违规扩展；2. 针对 static 类进行枚举化或功能封装重构，提升一致性；3. 设立架构守卫规则，自动化检查并持续集成改进流程，减少结构性风险积累。"
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: api_key.API_KEY,
        }
    },
    mounted(){
        this.antiPatternAdvice = normalizeData(this.antiPatternAdvice)
        this.ViolationInfoAdvice = normalizeData(this.ViolationInfoAdvice)

        this.antiPatternList = EventBus.antiPatternList
        this.ViolationInfoList = EventBus.ViolationInfoList
        this.totalSum();
        this.$refs.commonAntiPieChart.initPieChart({
            data: this.antiPatternList.map(item => ({
                value: Number(item.total),
                name: item.name,
            }))
        })
        this.$refs.ViolationInfoPieChart.initPieChart({
            data: Object.keys(EventBus.ruleTypeCounts).map((type) => ({
              value: EventBus.ruleTypeCounts[type],
              name: type,
            }))
        })
        console.log("this.antiPatternList")
        console.log(this.antiPatternList)
        console.log("this.ViolationInfoList")
        console.log(this.ViolationInfoList)
    },
    methods:{
        totalSum() {
            // 获取反模式总数量
            this.antiPatternTotalSum = this.antiPatternList.reduce((accumulator, currentObject) => {
                return accumulator + currentObject.total;
            }, 0); // 初始值为0

            // 获取自定义反模式总数量
            this.ViolationInfoListTotalSum = this.ViolationInfoList.reduce((acc, current) => {
                return acc + current.total;
            }, 0);
        },
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。给你的数据是最新版本的架构" +
        "通用反模式违规数据以及架构自定义反模式违规数据,请你据此来给出评分以及建议。首先是架构通用反模式违规数据：" + 
        + this.antiPatternList + "。" +
        "接下来是架构自定义反模式违规数据：" + this.ViolationInfoList +
        "接下来请你对该项目分别给出通用反模式和自定义架构反模式评分，范围为0-5分，可保留一位小数。并分别给出项目的建议。请按照以下json形式回复我：" +
        " { antiPatternRating : , antiPatternAdvice: {总体情况：、现存问题：、改进建议} , ViolationInfoRating : , ViolationInfoAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.antiPatternRating = this.response.antiPatternRating
          this.antiPatternAdvice = this.response.antiPatternAdvice
          this.ViolationInfoRating = this.response.ViolationInfoRating
          this.ViolationInfoAdvice = this.response.ViolationInfoAdvice
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
.section-div-div{
    display: flex;
    flex-direction: column;
    width: 100%;
    color: black;
}

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

.chart-title{
    font-size: 1.1rem;
}
</style>