<template>
    <section class="report-section">
        <h2>综合评价</h2>
        <div class="section-top">
            <el-row>
                <div class="section-div" style="height: 60px;">
                    <span class="title1">最终版本项目评分</span>
                    <el-rate v-model="finalRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                    score-template="{value}"></el-rate>
                </div>
            </el-row>
            <el-row type="flex" justify="space-between">
                <el-col :span="24 / 5" class="section-div">
                    <div class="title2">
                        <img src="@/assets/img/report/规则.png" alt="pmd" class="icon">代码质量检查
                    </div>
                    <div class="number2"><span>{{ pmdTotalSum }}</span></div>
                    <div class="type2">{{ pmdTypeSum }}种</div>
                    <div class="warn2">
                        <img src="@/assets/img/report/警告.png" alt="warn" class="icon">
                        最严重的高达{{ pmdWarn.total }}个
                    </div>
                    <div>
                        <el-rate v-model="pmdRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                        score-template="{value}" class="rate2"></el-rate>
                    </div>
                </el-col>
                <el-col :span="24 / 5" class="section-div">
                    <div class="title2">
                        <img src="@/assets/img/report/反模式.png" alt="anti-pattern" class="icon">通用反模式
                    </div>
                    <div class="number2"><span>{{ antiPatternTotalSum }}</span></div>
                    <div class="type2">7种</div>
                    <div class="warn2">
                        <img src="@/assets/img/report/警告.png" alt="warn" class="icon">
                        {{ antiPatternWarn.name}}高达{{ antiPatternWarn.total }}个
                    </div>
                    <div>
                        <el-rate v-model="antiPatternRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                        score-template="{value}" class="rate2"></el-rate>
                    </div>
                </el-col>
                <el-col :span="24 / 5" class="section-div">
                    <div class="title2">
                        <img src="@/assets/img/report/反模式.png" alt="anti-pattern" class="icon">自定义反模式
                    </div>
                    <div class="number2"><span>{{ ViolationInfoTotalSum }}</span></div>
                    <div class="type2">4种</div>
                    <div class="warn2">
                        <img src="@/assets/img/report/警告.png" alt="warn" class="icon">
                        最严重的高达{{ ViolationInfoWarn.total }}个
                    </div>
                    <div>
                        <el-rate v-model="ViolationInfoRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                        score-template="{value}" class="rate2"></el-rate>
                    </div>
                </el-col>
                <el-col :span="24 / 5" class="section-div">
                    <div class="title2">
                        <img src="@/assets/img/report/重构.png" alt="pmd" class="icon">重构检测
                    </div>
                    <div class="number2"><span>{{ refactTotalSum }}</span></div>
                    <div class="type2">{{ refactList.length }} 种</div>
                    <div class="warn2">
                        <img src="@/assets/img/report/警告.png" alt="warn" class="icon">
                        {{ refactWarn.name}}高达{{ refactWarn.total }}个
                    </div>
                    <div>
                        <el-rate v-model="refactRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                        score-template="{value}" class="rate2"></el-rate>
                    </div>
                </el-col>
                <el-col :span="24 / 5" class="section-div">
                    <div class="title2">
                        <img src="@/assets/img/report/反模式.png" alt="anti-pattern" class="icon">侵入式修改
                    </div>
                    <div class="number2"><span>{{ intrusiveTotalSum }}</span></div>
                    <div class="type2">{{ intrusiveList.length }} 种</div>
                    <div class="warn2">
                        <img src="@/assets/img/report/警告.png" alt="warn" class="icon">
                        {{ intrusiveWarn.name}}高达{{ intrusiveWarn.total }}个
                    </div>
                    <div>
                        <el-rate v-model="intrusiveRating" :colors="rateColors" disabled show-score text-color="#ff9900"
                        score-template="{value}" class="rate2"></el-rate>
                    </div>
                </el-col>
            </el-row>
        </div>
      </section>
</template>

<script>
import EventBus from '../eventBus'
import api_key from "../../../public/api_key.json"


export default {
    data(){
        return {
            finalRating: null,
            rateColors: ['#99A9BF', '#F7BA2A', '#FF9900'],

            pmdList: [],
            pmdTotalSum: 0,
            pmdTypeSum: 0,
            pmdWarn: {
                types: [],
                total: 0,
            },
            pmdRating: 3.4,

            antiPatternList: [],
            antiPatternTotalSum: 0,
            antiPatternWarn: {
                name: '',
                total: 0
            },
            antiPatternRating: 3.8,


            userInput: '',
            response: '',
            loading: false,
            API_KEY: api_key.API_KEY,
            ViolationInfoList: [],
            ViolationInfoTotalSum: 0,
            ViolationInfoWarn: {
                name: '',
                total: 0
            },
            ViolationInfoRating: 3.7,

            refactList: [],
            refactTotalSum: 0,
            refactWarn: {
                name: '',
                total: 0
            },
            refactRating: 3.5,

            intrusiveList: [],
            intrusiveTotalSum: 0,
            intrusiveWarn: {
                name: '',
                total: 0
            },
            intrusiveRating: 3.1,
        }
    },
    mounted(){
        this.pmdList = EventBus.pmdList
        this.antiPatternList = EventBus.antiPatternList
        this.ViolationInfoList = EventBus.ViolationInfoList
        this.refactList = EventBus.refactList
        this.intrusiveList = EventBus.intrusiveList

        this.finalRating = (this.pmdRating + this.antiPatternRating + this.ViolationInfoRating + this.refactRating + this.intrusiveRating) / 5

        this.totalSum();
        // console.log("this.ViolationInfoWarn")
        // console.log(this.ViolationInfoWarn)
        // console.log("this.intrusiveWarn")
        // console.log(this.intrusiveWarn)
        // console.log("this.refactWarn")
        // console.log(this.refactWarn)
        // console.log("this.intrusiveList")
        // console.log(this.intrusiveList)
        // console.log("EventBus.intrusiveList")
        // console.log(EventBus.intrusiveList)
    },
    methods:{
        totalSum() {
            //获取pmd
            this.pmdTotalSum = this.pmdList.reduce((sum, item) => sum + item.count, 0);
            this.pmdTypeSum = this.pmdList.length
            
            
            Object.values(this.pmdList).forEach(item => {
              if (item.priority === '3') {
                  if (!this.pmdWarn.types.includes(item.rule)) {
                      this.pmdWarn.types.push(item.type);
                  }
                  this.pmdWarn.total += item.count;
              }
            });

            // 获取反模式总数量
            this.antiPatternTotalSum = this.antiPatternList.reduce((accumulator, currentObject) => {
                return accumulator + currentObject.total;
            }, 0); // 初始值为0

            // 获取反模式最多的类别
            var maxTotalItem = this.antiPatternList[0];
            for (var i = 1; i < this.antiPatternList.length; i++) {
                if (this.antiPatternList[i].total > maxTotalItem.total) {
                    maxTotalItem = this.antiPatternList[i];
                }
            }
            this.antiPatternWarn = {
                name: maxTotalItem.name,
                total: maxTotalItem.total
            };

            // 获取自定义反模式
            this.ViolationInfoTotalSum = this.ViolationInfoList.reduce((accumulator, current) => {
                return accumulator + current.total;
            }, 0);
            maxTotalItem = this.ViolationInfoList[0];
            this.ViolationInfoWarn = {
                total: maxTotalItem.total
            };

            // 获取重构检测
            this.refactTotalSum = this.refactList.reduce((accumulator, current) => {
                return accumulator + current.total;
            }, 0);
            maxTotalItem = this.refactList[0];
            this.refactWarn = {
                name: maxTotalItem.type,
                total: maxTotalItem.total
            };

             // 获取侵入式反模式
             this.intrusiveTotalSum = this.intrusiveList.reduce((accumulator, currentObject) => {
                return accumulator + currentObject.count;
            }, 0);
            this.intrusiveList.sort((a,b)=>b.count-a.count)
            this.intrusiveWarn.name = this.intrusiveList[0].name
            this.intrusiveWarn.total = this.intrusiveList[0].count
            console.log("this.finalRating")
        console.log(this.finalRating)
        },

        async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        this.userInput = "你现在是软件架构高级工程师，现在需要让你来根据代码的扫描结果来进行评分、评分依据以及分析建议。一共分为五个维度：" + 
        "1.代码质量检查 2.架构通用反模式检查 3.自定义反模式 4.重构检测 5.侵入式修改检测。接下来我会分别给你这五个维度所检测出来的" + 
        "项目的违规种类和数目信息，请你根据这些信息来进行评分。满分5分，分数越高说明项目的质量越好。首先是1.代码质量检查的扫描结果：" + 
        "该项目总共违规的规则种类：" + this.pmdTypeSum + "。违规规则总数：" + this.pmdTotalSum + "。该项目违规Type为High的规则种类以及数目：" + 
        this.pmdWarn.types.length + "," + this.pmdWarn.total + "。接着是2.架构通用反模式检查的扫描结果：该项目总共违反的反模式种类以及数目：" + 
        this.antiPatternList.length + "，" + this.antiPatternTotalSum + "。其中违反种类最多的是：" + this.antiPatternWarn.name + "，数目为：" + this.antiPatternWarn.total +
        "。接下来是3.自定义反模式的扫描结果：自定义反模式违规信息为：" + this.ViolationInfoList + "。  接下来是4.重构检测的数据列表："+ this.refactList +  "。接下来是最后一个，5.侵入式修改的内容：" + 
        this.intrusiveList +
        "。接下来请你对该项目进行评分，分别给出项目总体分数和4部分的对应分数。范围均为0-5分，可保留一位小数。请按照以下json形式回复我：" + 
        " { pmdRating : , antiPatternRating : , ViolationInfoRating : , refactRating : , intrusiveRating}."

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
          this.pmdRating = this.response.pmdRating
          this.antiPatternRating = this.response.antiPatternRating
          this.ViolationInfoRating = this.response.ViolationInfoRating
          this.refactRating = this.response.refactRating
          this.intrusiveRating = this.response.intrusiveRating
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

.section-top .title1 {
  color: black;
  font-size: 1.25rem;
}

.section-top .title2 {
  color: black;
  font-size: 1.15rem;
}

.icon {
  /* vertical-align: middle; 垂直居中对齐 */
  margin-right: 8px; /* 与文本的间距 */
  width: 20px; /* 图标宽度 */
  height: 20px; /* 图标高度 */
}

.section-top .number2 {
  color: black;
  font-size: 1.25rem;
  font-weight: 600;
  padding: 10px;
}

.section-top .type2 {
  color: black;
  font-size: 1.15rem;
}

.section-top .warn2 {
  color: black;
  font-size: 1.15rem;
  height: 40px;
  padding: 10px 0;
}

::v-deep.el-rate .el-rate__icon {
  font-size: 40px;
}

::v-deep.rate2 .el-rate__icon {
  font-size: 25px;
}

.el-col {
  flex: 0 0 20%; /* 每列占据 20% 的宽度 */
  max-width: 20%; /* 确保列不会超出 20% 的宽度 */
}
</style>