<template>
    <div>
        <div class="div2-top">
            <h4 class="h4-title">重构检测</h4>
            <span class="text">代码重构是指不改变软件系统外部行为时对代码内部结构进行的改进操作，能够从语义层面反应代码变更；本章节主要分析不同版本代码之间的变更，这易导致侵入式反模式。</span>
            <span class="number">共 {{ refactList.length }} 种重构，共 {{ refactTotalSum }} 条重构信息。</span>
        </div>

        <div class="div2-bottom">
            <el-table :data="refactList" highlight-current-row>
                <el-table-column label="类型" prop="type"></el-table-column>
                <el-table-column label="解释" prop="desc"></el-table-column>
                <el-table-column label="节点数" prop="total" width="150" sortable></el-table-column>
            </el-table>
        </div>

        <div class="div2-chart">
            <span class="chart-title">重构类型统计</span>
            <pieChart ref="refactChart" style="height: 600px; width: 100%" />
        </div>

        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="refactRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ refactAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ refactAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ refactAdvice['改进建议'] }}</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import EventBus from "@/components/eventBus";
import pieChart from "../pieChart.vue";
import api_key from "../../../../public/api_key.json";
import { normalizeData } from "../normalizeData";

export default {
    components: { pieChart },
    data() {
        return {
            refactList: [],
            refactTotalSum: 0,
            simplifiedList: [],

            refactRating: 3.5,
            refactAdvice: {
                "总体情况": "该项目共检测到686次重构操作，说明团队有较强的代码维护意识。重构操作以提取方法、添加参数、变量提取等非侵入性结构优化为主，整体重构质量较高，未发现大规模破坏性操作，重构过程以增强代码可维护性和清晰度为目标。",
                "现存问题": "虽然多数重构操作合理且安全，但部分操作如频繁的访问修饰符更改（如属性、方法的访问控制修改超60次）、方法和变量的重命名等，也可能带来行为侵入性风险，若缺乏严格的回归测试支持，可能隐藏回归缺陷；另外较少出现类级别的高层次结构调整，如类移动、上提/下提操作等，表明结构优化可能局限在局部方法层面。",
                "改进建议": "1. 加强重构操作后的自动化测试覆盖率，尤其是对访问修饰符修改后的模块进行回归测试；2. 鼓励更多类级别的结构优化（如类解耦、职责分离），不仅限于方法和变量级重构；3. 规范重命名策略，结合统一命名规范与文档自动化工具，降低理解成本；4. 在持续集成流程中引入重构行为的质量控制机制，如审查或静态分析审计。"
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: api_key.API_KEY,
        };
    },
    mounted() {
        this.refactAdvice = normalizeData(this.refactAdvice);
        
        this.refactList = EventBus.refactList;
        this.refactTotalSum = this.refactList.reduce((accumulator, current) => {
            return accumulator + current.total;
        }, 0);
        this.$refs.refactChart.initPieChart({
            data: this.refactList.map((item) => ({
                value: item.total,
                name: item.type,
            })),
            legend: 'none'
        });
        this.simplifiedList = this.refactList.map(({ desc, total, type }) => ({ desc, total, type }));
        // console.log("this.simplifiedList")
        // console.log(this.simplifiedList)
        // console.log("this.refactTotalSum")
        // console.log(this.refactTotalSum)
    },
    methods:{
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。给你的数据是最新版本的重构检测分析" +
        "数据,主要分析不同版本代码之间的变更，这易导致侵入式反模式。请你据此来给出评分、评分依据以及分析建议。这是检测到的重构操作的总数：" + this.refactTotalSum + 
        + "。 这是重构检测操作信息的列表： " + this.simplifiedList + "。接下来请你对该项目给出评分，范围为0-5分，可保留一位小数。并给出评分依据以及分析建议。请按照以下json形式回复我：" +
        " { refactRating : ,refactAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.refactRating = this.response.refactRating
          this.refactAdvice = this.response.refactAdvice
        } catch (err) {
          this.response = '请求失败，请检查网络或 API 设置。'
        } finally {
          this.loading = false
        }
      },
    },
};
</script>

<style scoped>
.div2-top {
    width: 100%;
    display: flex;
    flex-direction: column;
    /* height: 200px; */
    /* margin-bottom: 5%; */
}

.div2-top span {
    display: flex;
    text-align: left;
    margin-bottom: 1%;
}

.div2-top .h4-title {
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