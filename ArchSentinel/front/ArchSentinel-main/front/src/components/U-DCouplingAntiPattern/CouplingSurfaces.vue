<template>
  <div
    class="CouplingSurfaces"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="right">
        <div class="right-table">
          <el-table
            class="right-table-in"
            :data="CFTable"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
          >
            <el-table-column
              label="ownership"
              prop="ownership"
            ></el-table-column>
            <el-table-column
              label="总数"
              prop="number"
              width="250"
              sortable
            ></el-table-column>
            <el-table-column label="含义" prop="explanation"></el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <button
                  link
                  @click.prevent="showSpecificType(scope.$index)"
                  class="btn"
                >
                  查看详情
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="right-chart">
          <div ref="couplingPieChart" style="width: 97%; height: 100%"></div>
        </div>
      </div>
    </div>
    <div>
      <!-- 细节表 -->
      <el-dialog
        title="耦合面详细上下游实体"
        :visible.sync="detail_Visible"
        height="600px"
        width="80%"
        @close="detailDataClose"
      >
        <div class="detail-dialog" style="background-color: transparent">
          <div class="detail-table" style="background-color: #f0f0f0">
            <el-table
              :data="tableData"
              highlight-current-row
              :cell-style="getCellStyle"
              :header-cell-style="getHeaderStyle"
              max-height="500px"
            >
              <el-table-column
                label="序号"
                prop="number"
                width="80"
              ></el-table-column>
              <el-table-column
                label="上游实体"
                prop="src_name"
                width="250"
                show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                label="上游实体类别"
                prop="src_category"
                width="140"
              ></el-table-column>
              <el-table-column
                label="上游实体所在文件"
                prop="src_file"
                show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                label="下游实体"
                prop="dest_name"
                show-overflow-tooltip
                width="250"
              ></el-table-column>
              <el-table-column
                label="下游实体类别"
                prop="dest_category"
                width="140"
              ></el-table-column>
              <el-table-column
                label="下游实体所在文件"
                prop="dest_file"
                show-overflow-tooltip
              ></el-table-column>
              <el-table-column label="操作" width="300">
                <template slot-scope="scope">
                  <button
                    size="medium"
                    :style="{ width: 'auto' }"
                    round
                    @click="showDetail(scope.row)"
                    class="btn"
                  >
                    查看上下游代码片段
                  </button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-pagination
            @size-change="sizeChange"
            @current-change="currentChange"
            :current-page="page"
            :page-size="size"
            :page-sizes="pageSizes"
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
          >
          </el-pagination>
          <div class="dialog-footer">
            <button class="btn" @click="detailDataClose">返回</button>
          </div>
        </div>
      </el-dialog>
    </div>
    <!-- 错误弹窗 -->
    <el-dialog
      title="错误提示!"
      :visible.sync="errorDialogVisible"
      width="30%"
      @close="handleDialogClose"
    >
      <div>
        <div class="dialog-error">
          <span>{{ errorMessage }}</span>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="handleDialogClose">确定</button>
        </div>
      </div>
    </el-dialog>
    <el-dialog
      title="实体代码展示"
      :visible.sync="showCodeDialog"
      width="80%"
      append-to-body
      header-align="center"
    >
      <div class="code-container">
        <!-- 上游代码 -->
        <div class="code-section">
          <h3>源实体代码</h3>
          <pre><code class="language-java" ref="upstreamCode" v-html="upstreamCode" ></code></pre>
        </div>
        <!-- 下游代码 -->
        <div class="code-section">
          <h3>目标实体代码</h3>
          <pre><code class="language-java" ref="downstreamCode" v-html="downstreamCode" ></code></pre>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <button @click="showCodeDialog = false" class="btn">关闭</button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import { getAnalysisPaths, resultUrls } from '../../services/analysisResults'
import EventBus from '../eventBus'

export default {
  props: {
    parentData: {
      type: Object,
      required: true, // 确保父组件传递此对象
    },
  },
  data() {
    return {
      // 将 props 数据复制到 data 中
      localParentData: { ...this.parentData }, // 使用展开运算符来复制对象
      upstreamFilePath: '',
      downstreamFilePath: '',
      dataLoading: false,
      errorDialogVisible: false, // 弹窗是否可见
      errorMessage: '', // 错误信息
      detail_Visible: false, //详细条目是否可见

      src_dest_result: [],
      show_Enre_Gap: false,
      showList: [],
      fileList: [],

      //表格数据
      CFTable: [],
      //详细数据
      detailTable: [],
      //为详细数据分页所需
      tableData: [], //当前页的表格
      page: 1, //第几页
      size: 10, //一页多少条
      total: 0, //总条目数
      pageSizes: [10, 20, 50, 100, 500, 1000], //可选择的一页多少条

      // 条目类别归属图（ownership类别）
      couplingPieChart: null,

      //代码片段
      showCodeDialog: false,
      upstreamCode: '',
      downstreamCode: '',
    }
  },
  mounted() {
    this.show_Enre_Gap = true
    this.handleJSONRead()
  },
  methods: {
    // 读取文件内容
    async handleJSONRead() {
      this.dataLoading = true

      // 清空存储对象
      try {
        this.src_dest_result = []
        // 从后端读取本次工具执行产生的耦合面结果
        const response = await axios.get(resultUrls.facade)
        const parsedData = response.data

        if (!parsedData.res || !Array.isArray(parsedData.res.e2n)) {
          console.error('错误的json结构: e2n not found')
          this.errorMessage = '错误的json结构: e2n not found' // 设置错误信息
          this.dataLoading = false
          this.errorDialogVisible = true // 显示弹窗
          return
        }
        // 遍历 e2n 数组
        parsedData.res.e2n.forEach((item) => {
          const dest = item.dest

          if (!dest) {
            console.warn(`Missing dest in entry, skipping:`, item)
            return
          }
          const type = dest.ownership // 使用 rawType 作为分类依据
          if (!this.src_dest_result[type]) {
            this.src_dest_result[type] = [] // 初始化类别数组
          }

          // 将 src和 dest 存储到对应ownership类别
          this.src_dest_result[type].push({
            src: {
              location: item.src.location,
              file: item.src.File,
              name: item.src.qualifiedName,
              ownership: item.src.ownership,
              category: item.src.category,
            },
            dest: {
              location: item.dest.location,
              file: item.dest.File,
              name: item.dest.qualifiedName,
              ownership: item.dest.ownership,
              category: item.dest.category,
              //refactor?:item.dest.refactor,
            },
          })
        })
        //展示第一张表，整体分类
        this.processTableData()
        //与此同时展示饼状图
        this.initPieChart()
        this.dataLoading = false
      } catch (error) {
        console.error('处理JSON结果出错:', error)
        this.errorMessage = error.message // 设置错误信息
        this.dataLoading = false
        this.errorDialogVisible = true // 显示弹窗
      }

      return false
    },
    //按照ownership展示整体分类
    processTableData() {
      this.CFTable = []
      if (this.src_dest_result['extensive']) {
        this.CFTable.push({
          ownership: 'extensive',
          number: this.src_dest_result['extensive'].length,
          explanation: '上游系统定义，下游系统完全复用的实体',
        })
      }
      if (this.src_dest_result['intrusive native']) {
        this.CFTable.push({
          ownership: 'intrusive native',
          number: this.src_dest_result['intrusive native'].length,
          explanation: '上游系统定义，下游系统复用并修改的实体',
        })
      }
      if (this.src_dest_result['actively native']) {
        this.CFTable.push({
          ownership: 'actively native',
          number: this.src_dest_result['actively native'].length,
          explanation: '上游系统中不存在，为下游系统扩展定义的实体',
        })
      }
      EventBus.CFList = this.CFTable;
    },

    //按照某一ownership展示细节每一条dest
    async showSpecificType(index) {
      let keys = Object.keys(this.src_dest_result)
      index = keys[index]
      if (!this.src_dest_result[index]) {
        console.log('error?')
        this.errorMessage = '没有找到 ownership排序在第${index+1}行的数据' // 设置错误信息
        this.dataLoading = false
        this.errorDialogVisible = true // 显示弹窗
        return
      }
      this.dataLoading = true
      if (this.src_dest_result[index].length > 500) {
        await new Promise((resolve) => setTimeout(resolve, 50))
      }
      let num = 0
      this.detailTable = this.src_dest_result[index].map((item) => ({
        number: (num += 1),
        src_name: item.src.name,
        src_file: item.src.file,
        dest_name: item.dest.name,
        dest_file: item.dest.file,
        src_category: item.src.category,
        dest_category: item.dest.category,
        src_location: item.src.location,
        dest_location: item.dest.location,
        src_ownership: item.src.ownership,
        dest_ownership: item.dest.ownership,
      }))
      this.dataLoading = false
      this.getTableData()
      this.detail_Visible = true
    },
    //展示上下游代码片段
    async showDetail(row) {
      this.upstreamCode = ''
      this.downstreamCode = ''

      this.dataLoading = true
      console.log('this.upstreamFilePath')
      console.log(this.upstreamFilePath)

      try {
        const paths = await getAnalysisPaths()
        this.upstreamFilePath = paths.upstreamFilePath
        this.downstreamFilePath = paths.downstreamFilePath
        // 调用后端接口获取上下游代码
        const response = await axios.post(
          '/api/getFacadeCode',
          {
            upProjectPath: this.upstreamFilePath,
            downProjectPath: this.downstreamFilePath,
            src_file: row.src_file, // 源实体文件
            src_start: row.src_location.startLine,
            src_end: row.src_location.endLine,
            src_ownership: row.src_ownership,
            dest_file: row.dest_file, // 源实体文件
            dest_start: row.dest_location.startLine,
            dest_end: row.dest_location.endLine,
            dest_ownership: row.dest_ownership,
          }
        )

        const data = response.data
        console.log(data)

        // 更新代码内容
        this.upstreamCode = data.upstreamCode || '无上游代码'
        this.downstreamCode = data.downstreamCode || '无下游代码'

        // 打开代码弹窗
        this.showCodeDialog = true
        this.dataLoading = false

        // 异步渲染代码高亮
        this.$nextTick(() => {
          this.highlightCode()
        })
      } catch (error) {
        console.error('获取代码片段失败:', error)
        const message = error.response?.data?.message || error.response?.data || error.message
        this.errorMessage = `获取代码片段失败：${message}` // 设置错误信息
        this.errorDialogVisible = true // 显示弹窗
      } finally {
        this.dataLoading = false
      }
    },
    highlightCode() {
      this.$nextTick(() => {
        // 确保 DOM 已更新，再获取 DOM 元素
        const upstreamCodeBlock = this.$refs.upstreamCode
        const downstreamCodeBlock = this.$refs.downstreamCode

        // 检查上游代码块是否存在并高亮
        if (upstreamCodeBlock) {
          hljs.highlightElement(upstreamCodeBlock)
        }

        // 检查下游代码块是否存在并高亮
        if (downstreamCodeBlock) {
          hljs.highlightElement(downstreamCodeBlock)
        }
      })
    },
    getTableData() {
      //allData为全部数据
      this.tableData = this.detailTable.slice(
        (this.page - 1) * this.size,
        this.page * this.size
      )
      this.total = this.detailTable.length
    },
    //page改变时的回调函数，参数为当前页码
    currentChange(val) {
      console.log('翻页，当前为第几页', val)
      this.page = val
      this.getTableData()
    },
    //size改变时回调的函数，参数为当前的size
    sizeChange(val) {
      console.log('改变每页多少条，当前一页多少条数据', val)
      this.size = val
      this.page = 1
      this.getTableData()
    },
    initPieChart() {
      // 初始化边类型饼图
      this.couplingPieChart = echarts.init(this.$refs.couplingPieChart)
      const pieOption = {
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
        title: {
          text: '耦合面下游实体ownership',
          left: 'center',
          top: 'top',
          textStyle: {
            color: '#000000',
            fontSize: '1.25rem',
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
          orient: 'horizontal',
          top: 'bottom',
          left: 'center',
          textStyle: {
            color: '#000000',
            fontSize: '1rem',
          },
        },
        series: [
          {
            name: '耦合面下游实体ownership',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000000',
                fontSize: '1.25rem',
              },
            },
            data: this.CFTable.map((item) => ({
              name: item.ownership,
              value: item.number,
            })),
          },
        ],
      }
      this.couplingPieChart.setOption(pieOption)
    },

    // 关闭错误弹窗
    handleDialogClose() {
      this.errorDialogVisible = false // 隐藏弹窗
    },
    // 关闭详细数据
    detailDataClose() {
      this.detail_Visible = false // 隐藏弹窗
    },

    toEner() {
      this.show_Enre_Gap = false
      this.$emit('close', { type: 'ener-java' })
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
.CouplingSurfaces {
  height: 100%;
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

.right {
  width: 100%;
  height: 100%;
  flex-direction: column;
  display: inline-block;
}

.right-table {
  width: 100%;
  height: 30%;
  display: flex;
}

.detail-table {
  width: 100%;
  height: 500px;
}

.right-table-in {
  width: 80%;
  /* margin-left: 10%; */
  margin: 0 5%;
  height: 100%;
}

.right-chart {
  margin-top: 2%;
  width: 100%;
  height: 55%;
  display: flex;
}

.dialog-main {
  height: 30%;
  padding: 2%;
}

.dialog-error {
  text-align: center;
  font-size: 1.5rem;
  margin-bottom: 20px;
  color: black;
}

.dialog-footer {
  text-align: center;
  text-align-last: center;
  margin-top: 20px;
  margin-bottom: 20px;
}

.dialog-main span {
  font-size: 1.25rem;
}

.dialog-main-btn {
  display: flex;
  justify-content: space-between;
  margin: 2% 3%;
}

.detail-dialog {
  height: 600px;
  display: flex;
  flex-direction: column;
}

::v-deep(.el-pagination__total),
::v-deep(.el-pagination__sizes),
::v-deep(.el-pagination__jump) {
  color: #000000;
}

.code-container {
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.code-section {
  width: 50%;
}

pre {
  background-color: #2d2d2d;
  color: #000000;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
}

h3 {
  text-align: center;
  color: #000000;
}

::v-deep(.el-input__inner) {
  height: 35px !important;
  line-height: 35px !important;
  color: black !important;
  font-size: 15px !important;
  background-color: transparent;
  border: 1px solid rgba(13, 14, 14, 0.5);
}

::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  color: black;
}
</style>
