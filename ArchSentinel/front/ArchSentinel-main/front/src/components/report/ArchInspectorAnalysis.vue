<template>
    <div class="section-div-div">
        <div class="div2-top">
            <h4 class="h4-title">代码质量检查</h4>
            <span class="text">代码质量检查旨在识别代码中的缺陷、不一致性、风险点和优化机会，以确保软件产品的健壮性和可靠性。</span>
            <span class="number">共识别出 {{pmdTypeSum}} 条违规规则，共 {{ pmdTotalSum }} 条违规</span>
        </div>

        <div class="div2-bottom">
            <table class="priority-table">
                <thead>
                    <tr>
                        <th style="width: 13%;">优先级</th>
                        <th style="width: 22%;">修复建议</th>
                        <th>种类占比</th>
                        <th>数量占比</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in pmdPriorityList" :key="item.priority">
                        <td>{{ getPriorityIcon(item.priority) }} Priority {{ item.priority }}</td>
                        <td>{{ getPriorityLabel(item.priority) }}</td>
                        <td>
                            <input
                            type="range"
                            class="custom-progress"
                            :id="'priority-type-' + item.priority"
                            :value="getTypePercentage(item.type)"
                            min="0"
                            max="100"
                            step="1"
                            disabled
                            :style="{
                                '--percent': getTypePercentage(item.type) + '%',
                                '--filled-color': getTypePercentage(item.type) >= 50 ? '#e74c3c' : '#3399ff'
                            }"
                            />
                            <span>{{ getTypePercentage(item.type).toFixed(2) }}%</span>
                        </td>
                        <td>
                            <input
                            type="range"
                            class="custom-progress"
                            :id="'priority-total-' + item.priority"
                            :value="getTotalPercentage(item.total)"
                            min="0"
                            max="100"
                            step="1"
                            disabled
                            :style="{
                                '--percent': getTotalPercentage(item.total) + '%',
                                '--filled-color': getTotalPercentage(item.total) >= 50 ? '#e74c3c' : '#3399ff'
                            }"
                            />
                            <span>{{ getTotalPercentage(item.total).toFixed(2) }}%</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="div2-chart">
            <div>
                <span class="chart-title">优先级为3的前六种规则</span>
                <pieChart ref="priority3PieChart" style="height: 600px; width: 100%;"/>
            </div>
            <div style="margin-top: 5%">
                <span class="chart-title">数量前20种规则</span>
                <barChart ref="countBarChart" style="height: 600px; width: 100%;"/>
            </div>
        </div>
        <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="ArchInspectorRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span style="text-align: left;">建议：</span>
                <div>
                    <div>
                      <h4 style="text-align: left;">总体情况：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ArchInspectorAdvice['总体情况'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">现存问题：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ArchInspectorAdvice['现存问题'] }}</pre>
                    </div>
                    <div>
                      <h4 style="text-align: left;">改进建议：</h4>
                      <pre style="white-space: pre-wrap; text-align: left;">{{ ArchInspectorAdvice['改进建议'] }}</pre>
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
import { CHAT_API_KEY } from '../../config/runtime';
import { normalizeData } from './normalizeData';

export default {
    components: {pieChart, barChart},
    data(){
        return {
            pmdList: [],
            pmdTotalSum: 0,
            pmdTypeSum: 0,
            pmdPriorityList: [],

            ArchInspectorRating: 3.4,
            ArchInspectorAdvice: {
                "总体情况": "当前项目的代码质量存在严重问题，违规规则种类繁多（73种），总违规次数高达1695次，其中高优先级（优先级3）的违规就有892条，占比超过50%。这表明项目在基本编码规范、错误处理、安全性、可维护性等方面存在较大隐患，整体代码健康状况较差。",
                "现存问题": "1. 高优先级违规规则占比较大，反映出项目存在严重的设计或实现缺陷，如频繁捕获Throwable、字段与方法重名、错误使用printStackTrace等。\n2. 资源未关闭、线程不安全的静态字段、空的if语句等问题频发，显示开发中缺乏严谨的代码审查和异常管理机制。\n3. 多项中优先级问题（如使用O(n)复杂度集合方法、公共可变静态字段等）也大量存在，影响性能与可维护性。\n4. 安全相关问题（如SQL注入风险、系统退出调用、非序列化对象写入Session）虽然数量不算最多，但影响较大，需重点关注。",
                "改进建议": "1. 优先修复高优先级违规问题，特别是那些可能导致系统崩溃或信息泄露的问题，如捕获Throwable、资源未关闭、错误日志输出等。\n2. 引入严格的代码审查制度和CI自动静态检查机制，确保所有提交均通过质量网关。\n3. 建议团队采用统一的代码规范（如阿里Java开发手册），并强制执行。\n4. 对于中低优先级的问题，可以按照模块或迭代逐步推进整改，结合SonarQube等工具做持续性治理。\n5. 开展开发团队的编码规范与安全开发培训，提升整体工程素养，避免重复出现相似问题。"
            },
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],
            API_KEY: CHAT_API_KEY

        }
    },
    mounted(){
        this.ArchInspectorAdvice = normalizeData(this.ArchInspectorAdvice)

        this.pmdList = EventBus.pmdList;
        this.totalSum();
        this.handlePriority3PieChart();
        this.handleCountBarChart();
        // console.log("this.pmdList")
        // console.log(this.pmdList)
        // console.log("this.pmdTotalSum")
        // console.log(this.pmdTotalSum)
        // console.log("this.pmdTypeSum")
        // console.log(this.pmdTypeSum)
        // console.log("this.pmdPriorityList")
        // console.log(this.pmdPriorityList)


    },
    methods:{
        handlePriority3PieChart(){
            // 过滤优先级为3的规则
            const priorityThreeRules = this.pmdList.filter(item => item.priority === '3');
            // 根据数量排序
            const sortedRules = priorityThreeRules.sort((a, b) => b.count - a.count);
            // 取数量前六的规则
            const topSixRules = sortedRules.slice(0, 6);
            // 转换为饼图数据格式
            const piedata = topSixRules.map(item => ({
                name: item.rule,
                value: item.count,
            }));

            this.$refs.priority3PieChart.initPieChart({
                data: piedata
            });
        },
        handleCountBarChart(){
            const topTwentyRules = this.pmdList.sort((a, b) => b.count - a.count).slice(0, 20);
            topTwentyRules.sort((a, b) => a.count - b.count)

            // 准备柱形图数据
            const labels = topTwentyRules.map(item => item.rule); // 规则名称作为标签
            const values = topTwentyRules.map(item => item.count); // 规则数量作为数据
            console.log('values', values)

            this.$refs.countBarChart.initBarChart({
                category: labels,
                data: values
            });
        },
        totalSum() {
            this.pmdTotalSum = this.pmdList.reduce((sum, item) => sum + item.count, 0);
            this.pmdTypeSum = this.pmdList.length;

            const priorityMap = {};
            this.pmdList.forEach(item => {
                const { priority, count } = item;
                if (!priorityMap[priority]) {
                    priorityMap[priority] = { type: 0, total: 0, priority };
                }
                priorityMap[priority].type += 1;
                priorityMap[priority].total += count;
            });
            this.pmdPriorityList = Object.values(priorityMap);
            this.pmdPriorityList.sort((a, b) => b.priority - a.priority);
        },
        getPriorityIcon(priority) {
            const icons = {
                3: '🚨',
                2: '⚠️',
                1: '🟡'
            };
            return icons[priority] || '';
        },
        getPriorityLabel(priority) {
            const labels = {
                3: '需立马修复',
                2: '强烈建议修复',
                1: '可根据项目规范决定是否处理'
            };
            return labels[priority] || '';
        },
        getTypePercentage(typeCount) {
            return (typeCount / this.pmdTypeSum) * 100;
        },
        getTotalPercentage(totalCount) {
            return (totalCount / this.pmdTotalSum) * 100;
        },
        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是代码审查工程师，现在需要让你来根据代码的扫描结果来进行评分以及建议。" + 
        "给你的数据是最新版本的代码的质量检查代码违规数据，请你据此来给出评分以及建议。 违规规则的种类数为：" +
        this.pmdTypeSum + "。 违规规则的总数为：" + this.pmdTotalSum + "。 违规规则的三种优先级，1-3，其中3为优先级最高，分别的数目为：" +
        this.pmdPriorityList + "。违规的规则详细介绍为：" + this.pmdList +  
        "。接下来请你对该项目依据上面的代码违规检测结果给出评分，范围为0-5分，可保留一位小数。并给出项目的建议。要求建议分为：1.总体情况、2.现存问题、3.改进建议。请按照以下json形式回复我：" + 
        " { ArchInspectorRating : , ArchInspectorAdvice : {总体情况：、现存问题：、改进建议}}."

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
          this.ArchInspectorRating = this.response.ArchInspectorRating
          this.ArchInspectorAdvice = this.response.ArchInspectorAdvice
        } catch (err) {
          this.response = '请求失败，请检查网络或 API 设置。'
        } finally {
          this.loading = false
        }
      },

    }
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

.div2-bottom .priority{
    display: flex;
    flex-direction: row;
    width: 100%;
    margin-bottom: 1%;
}

.div2-bottom .priority div{
    display: flex;
    width: 70%;
    margin-left: 1%;
    margin-top: 0.5%;
}

.div2-bottom .priority div input{
    display: flex;
}

.div2-bottom .priority div span{
    display: flex;
    width: 80%;
    margin-left: 1%;
}

.chart-title{
    font-size: 1.1rem;
}

input[type="range"].custom-progress {
  -webkit-appearance: none;
  width: 80%;
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(
    to right,
    var(--filled-color, #3399ff) 0%,
    var(--filled-color, #3399ff) var(--percent, 0%),
    #ddd var(--percent, 0%),
    #ddd 100%
  );
  outline: none;
}

/* 滑块样式 */
input[type="range"].custom-progress::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 14px;
  height: 14px;
  background: var(--filled-color, #3399ff);
  border-radius: 50%;
  cursor: pointer;
  margin-top: -4px;
}

input[type="range"].custom-progress::-moz-range-track {
  background: transparent;
}
input[type="range"].custom-progress::-moz-range-thumb {
  width: 14px;
  height: 14px;
  background: var(--filled-color, #3399ff);
  border: none;
  border-radius: 50%;
  cursor: pointer;
}

.priority-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-weight: normal; /* 全表统一字体 */
  border-top: 2px solid #333; /* 第一线：最顶线 */
}

.priority-table th,
.priority-table td {
  padding: 8px 12px;
  font-weight: normal; /* 表头不加粗 */
}

.priority-table thead tr {
  border-bottom: 2px solid #333; /* 第二线：表头底线 */
}

.priority-table tbody tr:last-child {
  border-bottom: 2px solid #333; /* 第三线：表格底线 */
}

.custom-progress {
  width: 100px;
  height: 6px;
  appearance: none;
  background: #eee;
  border-radius: 3px;
  margin-right: 8px;
  background-image: linear-gradient(to right, var(--filled-color) var(--percent), #eee var(--percent));
  background-size: 100% 100%;
  background-repeat: no-repeat;
  border: none;
}

</style>
