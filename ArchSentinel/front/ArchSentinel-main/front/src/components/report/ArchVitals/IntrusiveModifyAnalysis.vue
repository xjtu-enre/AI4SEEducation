<template>
    <div>
        <div class="div2-top">
            <h4 class="h4-title">侵入式修改</h4>
            <span class="text">目前上下游场景下具有耦合特征的反模式检测且呈现较少，本工具一定程度尚填补了该空缺，且提高了反模式检测效率。
                下游开发者为实现开展功能需求，在代码实现上可总结为接口访问和侵入式访问两种，结合具有十年以上下游安卓系统开发经历的架构师专家经验，侵入式都属于耦合反模式。
            </span>
            <span class="number">共 {{ intrusiveList.length }} 种侵入式反模式，共 {{ intrusiveTotalSum }} 条侵入式反模式。</span>
        </div>

        <div class="div2-bottom">
            <el-table :data="intrusiveList" highlight-current-row>
                <el-table-column prop="name" label="类型"/>
                <el-table-column prop="content" label="解释" />
                <el-table-column prop="count" label="条数" width="100" sortable/>
            </el-table>
        </div>

        <div class="div2-chart">
            <span class="chart-title">侵入式类型统计</span>
            <pieChart ref="intrusiveChart" style="height: 800px; width: 100%;"/>
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="intrusiveRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ intrusiveAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ intrusiveAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ intrusiveAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import EventBus from '@/components/eventBus';
import pieChart from '../pieChart.vue';
import { CHAT_API_KEY } from '../../../config/runtime';
import { normalizeData } from '../normalizeData';

export default {
    components: {pieChart},
    data(){
        return{
            intrusiveList: [],
            intrusiveTotalSum: 0,
            intrusiveTypeCounts: {},

            intrusiveRating: 3.1,
            intrusiveAdvice: {
                "总体情况": "本次扫描共发现侵入式修改 2 572 处，说明下游代码对上游代码有较大规模的直接改动或紧耦合扩展行为。其中字段增删、调用增删和方法参数调整最为频繁，占全部侵入式操作的 80% 以上，显示出较强的“直接改动上游实现”特征，架构稳定性和升级兼容性面临风险。",
                "现存问题": [
                    "1. 字段层面侵入严重：新增字段 741 次、删除字段 84 次、字段取值修改 94 次，表明类的内部状态被下游大量篡改，破坏封装，增加版本升级冲突概率。",
                    "2. 调用链被大幅插拔：新增调用 627 次、删除调用 588 次，暗示业务逻辑高度嵌入上游实现，若上游 API 变更将导致下游回归成本陡增。",
                    "3. 代码演化缺少契约化：方法签名改动（新增/修改/删除参数共 103 次）和可访问性调整（类/方法/变量共 51 次）频繁发生，说明缺乏稳定的接口层，对外行为容易破坏调用方假设。",
                    "4. 元数据一致性不足：类、方法、变量注解被修改 18 次，可能导致框架或注解驱动逻辑的不一致，增加运行时异常风险。"
                ],
                "改进建议": [
                    "1. 建立清晰的扩展点：通过 SPI/插件化、AIDL 或反射白名单等机制向下游暴露稳定接口，杜绝直接修改上游源代码和字段。",
                    "2. 推行接口契约治理：接口层采用版本号或兼容策略，配合 API 变更的 Diff 检测与审查，防止随意增删参数和修改可见性。",
                    "3. 引入侵入式检测闸口：在 CI 流水线中集成侵入式修改扫描，设置阈值（如单次提交侵入式修改 ≤5），超限则要求架构评审。",
                    "4. 加强封装与配置化：将需要变动的行为转移到配置、策略或回调中，避免硬编码调用和字段访问；对必须修改的字段提供 getter/setter 或数据中心模式。",
                    "5. 编写回归与契约测试：针对高频侵入类别（字段增删、调用增删）补齐自动化测试，确保上游升级时能第一时间发现破坏性变更。"
                ]
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: CHAT_API_KEY,
        }
    },
    mounted(){
        this.intrusiveAdvice = normalizeData(this.intrusiveAdvice);

        this.intrusiveList = EventBus.intrusiveList
        this.intrusiveTypeCounts = EventBus.intrusiveTypeCounts

        this.intrusiveTotalSum = this.intrusiveList.reduce((accumulator, currentObject) => {
            return accumulator + currentObject.count;
        }, 0);

        this.$refs.intrusiveChart.initPieChart({
            data: Object.keys(this.intrusiveTypeCounts).map((type) => ({
              value: this.intrusiveTypeCounts[type],
              name: type,
            }))
        })

        // console.log("this.intrusiveTotalSum")
        // console.log(this.intrusiveTotalSum)
        // console.log("this.intrusiveTypeCounts")
        // console.log(this.intrusiveTypeCounts)
        // console.log("this.intrusiveList")
        // console.log(this.intrusiveList)

    },

    methods:{
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是代码架构工程师，现在需要让你来根据代码的扫描结果来进行评分以及建议。给你的数据是最新版本的侵入式修改检测结果：" +
        "目前上下游场景下具有耦合特征的反模式检测且呈现较少，本工具一定程度尚填补了该空缺，且提高了反模式检测效率。" +
        "下游开发者为实现开展功能需求，在代码实现上可总结为接口访问和侵入式访问两种，结合具有十年以上下游安卓系统开发经历的架构师专家经验，" +
        "侵入式都属于耦合反模式。我现在给你侵入式修改的检测结果,请你据此来给出评分以及建议。首先是侵入式总数的数据：" + 
        this.intrusiveTotalSum + "。接下来是侵入式各个类别的数目统计：" + this.intrusiveTypeCounts  + 
        "请你对该项目给出评分，范围为0-5分,结果保留一位小数。并给出项目的建议。请按照以下json形式回复我：" +
        " { intrusiveRating : ,intrusiveAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.intrusiveRating = this.response.intrusiveRating
          this.intrusiveAdvice = this.response.intrusiveAdvice
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
