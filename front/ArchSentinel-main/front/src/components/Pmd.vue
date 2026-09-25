<template>
  <div
    class="Pmd"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="left">
        <div style="margin-top: 1%">
          <div
            class="upload-container"
          >
            <!-- 违规规则图表 -->
            <div class="chart-container">
              <div ref="rulePieChart" style="width: 100%; height: 200px;"></div>
            </div>
            <div class="chart-container">
              <div ref="ruleChart" style="width: 100%; height: 900px;"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="right">
        <div class="top-table">
          <!-- 第一个页面: 显示规则的表格 -->
          <div v-if="currentPage === 'rulePage'" class="rule-page">
            <el-table
              :data="pmdData"
              highlight-current-row
              height="100%"
              :cell-style="getCellStyle"
              :header-cell-style="getHeaderStyle"
            >
              <el-table-column
                type="index"
                label="ID"
                width="80"
                :index="(index) => index + 1"
              />
              <el-table-column
                prop="rule"
                label="规则"
                width="400"
              ></el-table-column>
              <el-table-column
                prop="description"
                label="描述"
              ></el-table-column>
              <el-table-column
                prop="priority"
                label="优先级"
                width="100"
                sortable
              ></el-table-column>
              <el-table-column
                prop="count"
                label="条数"
                width="100"
                sortable
              ></el-table-column>
              <el-table-column label="操作" width="250">
                <template slot-scope="scope">
                  <button class="btn" @click="showSpecificRule(scope.row)">
                    查看详情
                  </button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 第二个页面: 显示具体规则详情的表格 -->
          <div v-if="currentPage === 'detailPage'" class="detail-page">
            <div class="detail-page-table">
              <el-table
                :data="currentRuleData"
                highlight-current-row
                height="100%"
                :cell-style="getCellStyle"
                :header-cell-style="getHeaderStyle"
              >
                <el-table-column
                  type="index"
                  label="ID"
                  width="80"
                  :index="(index) => index + 1"
                />
                <el-table-column prop="file" label="文件路径"></el-table-column>
                <el-table-column
                  prop="line"
                  label="行号"
                  width="100"
                ></el-table-column>
                <el-table-column label="操作" width="250">
                  <template slot-scope="scope">
                    <button class="btn" @click="showSpecificCode(scope.row)">
                      查看详情
                    </button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="detail-page-btn">
              <button type="primary" @click="goBack" class="btn" style="margin-bottom: 1%;">返回</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      title="PMD违规代码展示"
      :visible.sync="showCodeDialog"
      width="40%"
      append-to-body
      header-align="center"
    >
      <div class="code-container">
        <!-- PMD违规规则代码 -->
        <div class="code-section">
          <h3>PMD违规规则代码</h3>
          <pre><code class="language-java" ref="PMDCode" v-html="PMDCode"></code></pre>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showCodeDialog = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Papa from 'papaparse'
import * as echarts from 'echarts'
import axios from 'axios'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import EventBus from './eventBus'

export default {
  data() {
    return {
      currentPage: 'rulePage', // 默认显示规则页面
      pmdData: [], // 用于存储表格展示的解析数据
      currentRule: null, // 当前查看的规则类型
      currentRuleData: [], // 当前规则类型的详细数据
      dataLoading: false,
      showCodeDialog: false,
      ruleData: [],
      PMDCode: '',
    }
  },
  mounted() {
    this.loadPmdCsv() // 加载 CSV 文件
  },
  methods: {
    // 读取 public 文件夹中的 pmd.csv 文件
    async loadPmdCsv() {
      try {
        const response = await fetch('/pmd.csv')
        const csvContent = await response.text()

        // 使用 Papa.parse 解析 CSV 数据
        const parsedData = Papa.parse(csvContent, {
          header: true,
          skipEmptyLines: true,
        })

        const data = parsedData.data

        // 按 rule 分类统计条数，并生成表格数据
        const ruleCounts = {}
        // 用于存储分组后的详细数据
        const ruleData = {}
        data.forEach((row) => {
            const rule = row.Rule || '未知规则'
            const description = row.Description || '无描述'
            const priority = row.Priority || '无优先级'
            const file = row.File || '未知文件'
            const line = row.Line || '未知行号'

            if (!ruleCounts[rule]) {
                ruleCounts[rule] = { description, priority, count: 0 }
                ruleData[rule] = []
            }
            ruleCounts[rule].count += 1
            ruleData[rule].push({ file, line, priority })
        })

        // 转换为表格展示的数据结构
        this.pmdData = Object.keys(ruleCounts).map((rule) => ({
          rule,
          description: ruleCounts[rule].description,
          priority: ruleCounts[rule].priority,
          count: ruleCounts[rule].count,
        }))

        EventBus.pmdList = this.pmdData

        // 先按照priority排序，然后在每个priority中按照count排序
        this.pmdData.sort((a, b) => {
            // 比较priority
            if (a.priority !== b.priority) {
              return b.priority.localeCompare(a.priority); // '3' '2' '1'的顺序
            }
            // 如果priority相同，则比较count
            return b.count - a.count; // 从大到小排序
        });

        // 存储分组后的详细数据
        this.ruleData = ruleData

        // 对规则类型按数量排序，并取前6个
        const topRules = Object.entries(ruleCounts)
          .sort((a, b) => a[1].count - b[1].count) // 按数量降序排序
          // .slice(0, 6) // 取前6个
          .map(([rule, data]) => ({
            rule,
            count: data.count,
          }))

        // 更新图表数据
        this.updateRulePieChart();
        this.updateRuleChart(topRules)
      } catch (error) {
        console.error('加载 pmd.csv 文件失败:', error)
        this.$message.error('加载 pmd.csv 文件失败！')
      }
    },
    updateRulePieChart(){
      const chart = echarts.init(this.$refs.rulePieChart);

      const priorityData = this.pmdData.reduce((acc, item) => {
        if (!acc[item.priority]) {
          acc[item.priority] = { priority: item.priority, total: 0 };
        }
        acc[item.priority].total += item.count;
        return acc;
      }, {});

      // 转换为饼图所需的数据格式
      const pieData = Object.values(priorityData).map(item => ({
        name: `Priority ${item.priority}`,
        value: item.total,
      }));

      const pieOption = {
        title: {
          text: '违规规则优先级的数量占比',
          left: 'center',
          textStyle: {
            color: '#000000',
            fontSize: '1.1rem',
          },
        },
          toolbox: {
              itemSize: 20,
              iconStyle: {
                  borderColor: '#000',
              },
              feature: {
                  dataView: {},
                  restore: {},
                  saveAsImage: {},
              },
          },
          tooltip: {
              trigger: 'item',
              formatter: '{b}: {c} ({d}%)',
              textStyle: {
                  fontSize: 18,
              },
          },
          legend: {
              top: 'bottom',
              show: !pieData.hasOwnProperty('legend'),
          },
          series: [
              {
                  type: 'pie',
                  radius: ['40%', '70%'],
                  avoidLabelOverlap: false,
                  label: {
                     show: false,
                  },
                  data: pieData,
              },
          ],
      }
      chart.setOption(pieOption)
    },

    updateRuleChart(topRules) {
      const chart = echarts.init(this.$refs.ruleChart);
      const option = {
        title: {
          text: '违规规则统计',
          left: 'center',
          textStyle: {
            color: '#000000',
            fontSize: '1.25rem',
          },
        },
        toolbox: {
              itemSize: 20,
              iconStyle: {
                  borderColor: '#000',
              },
              feature: {
                  dataView: {},
                  restore: {},
                  saveAsImage: {},
              },
          },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
          formatter: '{b}: {c}',
        },
        grid: {
          left: '5%', // 调整y轴所占的宽度比例
          containLabel: true, // 确保标签包含在绘图区域内
        },
        xAxis: {
          type: 'value',
          name: '数量',
          axisLabel: {
            formatter: '{value}',
          },
        },
        yAxis: {
          type: 'category',
          data: topRules.map((item) => item.rule),
          axisTick: {
            show: false
          },
          axisLabel: {
            interval: 0,
            // rotate: 45,
          },
        },
        series: [
          {
            name: '规则统计',
            type: 'bar',
            label: {
              show: true,
              position: 'right'
            },
            data: topRules.map((item) => item.count),
            itemStyle: {
              color: '#5470c6',
            },
          },
        ],
      };
      chart.setOption(option);
    },

    // 查看规则详情
    showSpecificRule(row) {
      // console.log('查看规则详情')
      // console.log(row)
      // console.log('over')
      this.currentPage = 'detailPage' // 跳转到规则详情页面
      this.currentRule = row.rule // 设置当前规则
      this.currentRuleData = this.ruleData[row.rule] // 根据规则类型过滤数据
    },

    // 查看规则详情
    async showSpecificCode(row) {
      this.PMDCode = ''
      this.dataLoading = true

      try {
        // 调用后端接口获取上下游代码
        const response = await axios.post(
          '/api/getPMDCode',
          {
            // 当前表格信息
            file: row.file,
            line: row.line,
          }
        )
        const data = response.data

        // 更新代码内容
        this.PMDCode = data.PMDCode || '无PMD代码'

        // 打开代码弹窗
        this.showCodeDialog = true
        this.$message.success('代码片段加载成功')

        // 异步渲染代码高亮
        this.$nextTick(() => {
          this.highlightCode()
        })
      } catch (error) {
        console.error('获取代码片段失败:', error)
        const message = error.response?.data?.message || error.message
        this.$message.error(`获取代码片段失败：${message}`)
      } finally {
        this.dataLoading = false
      }
    },

    // 返回到主表格
    goBack() {
      this.currentPage = 'rulePage' // 返回到规则页面
      this.currentRule = null // 重置当前规则
      this.currentRuleData = [] // 清空详细数据
    },

    // 代码高亮
    highlightCode() {
      this.$nextTick(() => {
        // 确保 DOM 已更新，再获取 DOM 元素
        const PMDCodeBlock = this.$refs.PMDCode

        // 检查上游代码块是否存在并高亮
        if (PMDCodeBlock) {
          hljs.highlightElement(PMDCodeBlock)
        }
      })
    },

    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
  },
}
</script>

<style scoped>
.Pmd {
  height: 95vh;
  width: 100%;
  background-color: #f0f0f0;
  display: inline-block;
}

.main {
  height: 100%;
  width: 100%;
  margin: 1% 0 0 0;
  display: flex;
}

.left {
  width: 20%;
  height: 96%;
  margin: 0 1% 0 0.5%;
  flex-direction: column;
  display: inline-block;
  border: thin solid white;
}

.right {
  width: 78%;
  height: 96%;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 禁止滚动 */
}

.upload-container {
  display: flex;
  flex-direction: column;
  overflow:scroll;
  gap: 20px;
  margin-top: 20px;
}

.upload-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hidden-intrusive {
  display: none;
}
.styled-input {
  display: inline-block;
  padding: 5px 10px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  outline: none;
}

.styled-input:hover {
  background-color: #e6e6e6;
  border-color: #c0c0c0;
}

.top-table {
  width: 100%;
  height: 100%;
  /* overflow-y: auto; 启用滚动 */
  /* transition: none; 禁止过渡动画 */
}
.rule-page {
  height: 100%; /* 固定高度 */
  overflow: hidden; /* 禁止滚动 */
}

.detail-page {
  height: 100%; /* 固定高度 */
  overflow: hidden; /* 禁止滚动 */
}

.detail-page-table {
  height: 95%;
}

.detail-page-btn {
  height: 5%;
  padding-top: 0.5%;
  display: flex; 
  justify-content: flex-end;
  padding-right: 3.3%;
}

.down-table {
  margin-top: 1.5%;
  width: 97%;
  height: 50%;
  overflow-y: auto; /* 启用滚动 */
  height: 100%; /* 确保表格容器占据父元素的全部高度 */
  transition: none; /* 禁止过渡动画 */
}

.dialog-main {
  height: 10%;
  padding: 2%;
}

.dialog-main span {
  font-size: 1.25rem;
}

.dialog-main-btn {
  display: flex;
  justify-content: space-between;
  margin: 2% 3%;
}

.code-container {
  display: flex; /* 使用 flex 布局 */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 设置容器高度为 100%，根据需要调整 */
}

.code-section {
  max-width: 90%; /* 限制代码区域的最大宽度 */
  padding: 0px; /* 添加一些内边距 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 添加阴影，使代码部分更突出 */
  border-radius: 8px; /* 圆角效果 */
}

pre {
  background-color: #2d2d2d;
  color: #ffffff;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
}

h3 {
  text-align: center;
  color: #000000;
}
</style>
