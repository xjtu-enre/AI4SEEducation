<!-- eslint-disable no-unused-vars -->
<template>
  <div class="category">
    <div class="main">
      <div class="left">
        <div class="left-half">
          <div ref="pieChart" style="width: 97%; height: 100%"></div>
        </div>
      </div>

      <div class="right">
        <div class="right-table">
          <el-table
            :data="refactTable"
            highlight-current-row
            height="100%"
            :cell-style="getCellStyle"
            :header-cell-style="getHeaderStyle"
          >
            <el-table-column label="类型" prop="type"></el-table-column>
            <el-table-column label="解释" prop="desc"></el-table-column>
            <el-table-column label="节点数" prop="total" sortable></el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <button class="btn" @click="showDetailInfo(scope.row)">
                  查看详情
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <el-dialog
      title="重构详细信息"
      :visible.sync="detailDialogVisible"
      custom-class="adaptive-dialog"
      :width="dialogWidth"
      :top="dialogTop"
    >
      <div
        style="
          display: flex;
          flex-direction: column;
          height: 100%;
        "
      >
        <!-- 表格部分 -->
        <el-table
          :data="paginatedDetail"
          highlight-current-row
          style="flex: 1; width: 100%; height: 500px; background-color: #f0f0f0"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <!-- 序号列 -->
          <el-table-column label="序号" width="80">
            <template slot-scope="scope">
              {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="description" min-width="200">
            <template slot-scope="scope">
              <el-tooltip
                class="item"
                effect="dark"
                :content="scope.row.description"
                placement="top"
              >
                <div class="description-cell">
                  {{ scope.row.description }}
                </div>
              </el-tooltip>
            </template>
          </el-table-column>

          <el-table-column
            label="重构前代码数"
            prop="leftSideLocations.length"
            width="250"
          ></el-table-column>
          <el-table-column
            label="重构后代码数"
            prop="rightSideLocations.length"
            width="250"
          ></el-table-column>
          <el-table-column label="操作" width="250">
            <template slot-scope="scope">
              <button class="btn" @click="handleCodeClick(scope.row, 1)">
                查看详情
              </button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 弹窗内容 -->
        <el-dialog
          title="代码重构对比"
          :visible.sync="codeInfoVisible"
          append-to-body
          :modal="true"
          :close-on-click-modal="false"
          width="80%"
        >
          <div v-if="loading" style="text-align: center">
            <el-spinner size="large">加载中...</el-spinner>
          </div>
          <div style="display: flex; gap: 16px; background-color: white">
            <!-- 重构前代码 -->
            <div style="flex: 1">
              <h3 style="text-align: center; color: #000000">重构前代码</h3>
              <el-collapse>
                <el-collapse-item
                  v-for="(snippet, index) in beforeCode"
                  :key="'before-' + index"
                  :name="index"
                >
                  <template #title>片段 {{ index + 1 }}</template>
                  <pre
                    style="background-color: #f0f0f0"
                    class="code-block"
                    v-html="highlightCode(snippet)"
                  ></pre>
                </el-collapse-item>
              </el-collapse>
            </div>

            <!-- 重构后代码 -->
            <div style="flex: 1; background-color: #ababab">
              <h3 style="text-align: center; color: #000000">重构后代码</h3>
              <el-collapse class="afterElCollapse">
                <el-collapse-item
                  v-for="(snippet, index) in afterCode"
                  :key="'after-' + index"
                  :name="index"
                >
                  <template #title>片段 {{ index + 1 }}</template>
                  <pre
                    class="code-block"
                    v-html="highlightCode(snippet)"
                    style="background-color: #f0f0f0"
                  ></pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>

          <!-- 弹窗底部 -->
          <span slot="footer" class="dialog-footer">
            <el-button @click="closeDialog(1)">关闭</el-button>
          </span>
        </el-dialog>

        <!-- 分页控件 -->
        <el-pagination
          class="custom-pagination"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="selectedDetail.length"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[15, 20, 50]"
          style="margin-top: 1%; text-align: right"
        >
        </el-pagination>

        <!-- 按钮部分 -->
        <div style="text-align: center; margin: 1%">
          <button type="primary" @click="closeDialog(0)" class="btn">关闭</button>
        </div>
      </div>
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

const DETAILDIALOG = 0
const CODEDIALOG = 1
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
      showEnerGap: false,
      upstreamFilePath: '',
      downstreamFilePath: '',
      showList: [],
      fileList: [],

      jsonData: null,
      refactTable: null,
      refactoringStats: null,
      pieChart: null,
      detailDialogVisible: false, // 控制详情对话框显示
      selectedDetail: [], // 存储选中类型的详情信息
      currentPage: 1, // 当前页码
      pageSize: 15, // 每页显示条目数
      paginatedDetail: [], // 当前分页数据
      dialogWidth: '60%', // 默认宽度
      dialogTop: '10%', // 默认顶部位置
      codeInfoVisible: false,
      beforeCode: [
        `public class Example {
  public void method() {
    System.out.println("Before refactoring");
  }
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
        `if (condition) {
  executeAction();
}`,
      ],
      // 示例数据：重构后的代码片段
      afterCode: [
        `public class Example {
  public void method() {
    Logger.log("After refactoring");
  }
}`,
        `if (condition) {
  executeImprovedAction();
}`,
      ],
      loading: false,
    }
  },

  mounted() {
    this.showEnerGap = true
    this.init()
    this.updateDialogSize() // 初始时设置弹窗尺寸
    window.addEventListener('resize', this.updateDialogSize) // 监听窗口调整
  },
  methods: {
    init() {
      axios.get(resultUrls.refactor).then((res) => {
        // this.jsonData = res.data.commits;
        const commits = res.data.commits || []
        if (commits.length > 0) {
          this.jsonData = commits[0].refactorings
        } else {
          console.log('No commits found.')
        }
        //this.processPieData();
        this.processTableData()
        // this.dataLoading = false;
        this.initPieChart()
      })
    },
    initPieChart() {
      // 初始化饼图实例
      this.pieChart = echarts.init(this.$refs.pieChart)

      // 获取数据：基于 refactTable 中的 total 计算每种类型的占比
      const pieData = this.refactTable.map((item) => ({
        value: item.total,
        name: item.type,
      }))

      // 配置饼图选项
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
          text: '重构类型统计',
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
        // legend: {
        //     orient: 'horizontal',
        //     top: 'bottom',
        //     left: 'center',
        //     textStyle: {
        //         color: '#000000', // 设置文字颜色为黑色
        //         fontSize: '1rem', // 设置文字大小为1.25rem
        //     },
        // },
        series: [
          {
            name: '重构类型分布',
            type: 'pie',
            radius: ['20%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              formatter: '{b}',
              textStyle: {
                color: '#000000', // 设置文字颜色为黑色
                fontSize: '0.75rem', // 设置文字大小为1.25rem
              },
            },
            data: pieData, // 使用 refactTable 的数据
          },
        ],
      }

      // 设置选项并渲染图表
      this.pieChart.setOption(pieOption)
    },
    processTableData() {
      if (!Array.isArray(this.jsonData)) {
        console.error(
          'jsonData is not an array or is undefined:',
          this.jsonData
        )
        return
      }
      const typeStats = this.jsonData.reduce((acc, refactoring) => {
        const type = refactoring.type
        if (!acc[type]) {
          acc[type] = { count: 0, items: [] }
        }
        acc[type].count++
        acc[type].items.push(refactoring)
        return acc
      }, {})

      this.refactTable = Object.entries(typeStats).map(([type, data]) => ({
        type,
        desc: this.getDescByType(type),
        total: data.count,
        items: data.items,
      }))

      this.refactTable = [...this.refactTable].sort((a, b) => b.total - a.total)

      EventBus.refactList = this.refactTable
    },
    getDescByType(type) {
      const typeDescriptions = {
        'Extract Method': '提取方法',
        'Add Parameter': '添加参数',
        'Extract Variable': '提取变量',
        'Parameterize Attribute': '参数化属性',
        'Change Attribute Access Modifier': '更改属性访问修饰符',
        'Remove Attribute Modifier': '移除属性修饰符',
        'Rename Attribute': '重命名属性',
        'Rename Method': '重命名方法',
        'Parameterize Variable': '参数化变量',
        'Rename Variable': '重命名变量',
        'Change Method Access Modifier': '更改方法访问修饰符',
        'Remove Variable Modifier': '移除变量修饰符',
        'Change Variable Type': '更改变量类型',
        'Rename Parameter': '重命名参数',
        'Add Variable Modifier': '添加变量修饰符',
        'Add Attribute Annotation': '添加属性注解',
        'Remove Parameter': '移除参数',
        'Change Attribute Type': '更改属性类型',
        'Change Return Type': '更改返回类型',
        'Add Method Annotation': '添加方法注解',
        'Encapsulate Attribute': '封装属性',
        'Replace Variable With Attribute': '用属性替换变量',
        'Extract Attribute': '提取属性',
        'Inline Method': '内联方法',
        'Replace Attribute With Variable': '用变量替换属性',
        'Add Attribute Modifier': '添加属性修饰符',
        'Invert Condition': '反转条件',
        'Split Conditional': '拆分条件',
        'Remove Thrown Exception Type': '移除抛出的异常类型',
        'Add Method Modifier': '添加方法修饰符',
        'Merge Conditional': '合并条件',
        'Inline Variable': '内联变量',
        'Move Code': '移动代码',
        'Pull Up Attribute': '上提属性',
        'Modify Class Annotation': '修改类注解',
        'Add Parameter Annotation': '添加参数注解',
        'Remove Class Modifier': '移除类修饰符',
        'Change Class Access Modifier': '更改类访问修饰符',
        'Remove Method Annotation': '移除方法注解',
        'Move And Rename Class': '移动并重命名类',
        'Modify Method Annotation': '修改方法注解',
        'Remove Method Modifier': '移除方法修饰符',
        'Add Thrown Exception Type': '添加抛出的异常类型',
        'Inline Attribute': '内联属性',
        'Modify Attribute Annotation': '修改属性注解',
        'Change Parameter Type': '更改参数类型',
        'Remove Attribute Annotation': '移除属性注解',
      }
      return typeDescriptions[type] || '未知类型'
    },
    showDetailInfo(row) {
      this.selectedDetail = row.items // 将选中的数据传递给selectedDetail
      this.updatePagination() // 更新分页数据
      this.detailDialogVisible = true // 打开详情对话框
    },

    closeDialog(dialog) {
      //this.detailDialogVisible = false; // 关闭对话框
      if (dialog === DETAILDIALOG) {
        this.detailDialogVisible = false
      } else if (dialog === CODEDIALOG) {
        this.codeInfoVisible = false
      }
    },
    async handleCodeClick(row) {
      this.loading = true
      this.codeInfoVisible = true
      try {
        const paths = await getAnalysisPaths()
        this.upstreamFilePath = paths.upstreamFilePath
        this.downstreamFilePath = paths.downstreamFilePath
        // 调用后端接口获取上下游代码
        const response = await axios.post(
          '/api/getRefactorCode',
          {
            upProjectPath: this.upstreamFilePath,
            downProjectPath: this.downstreamFilePath,
            entity: row, // 传递前端的整个实体信息，包括 leftSideLocations 和 rightSideLocations
          }
        )

        // 动态加载重构前后代码
        const { beforeCode, afterCode } = response.data
        this.beforeCode = beforeCode // 后端返回的 beforeCode 数组
        this.afterCode = afterCode // 后端返回的 afterCode 数组
      } catch (error) {
        const message = error.response?.data?.message || error.response?.data || error.message
        this.$message.error('加载代码失败：' + message)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(size) {
      this.pageSize = size // 更新每页条目数
      this.updatePagination()
    },
    handleCurrentChange(page) {
      this.currentPage = page // 更新当前页码
      this.updatePagination()
    },
    updatePagination() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.paginatedDetail = this.selectedDetail.slice(start, end) // 更新当前页数据
    },
    toEner() {
      this.showEnerGap = false
      this.$emit('close', { type: 'ener-java' })
    },
    getHeaderStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    getCellStyle() {
      return 'background-color:transparent; color: #000000; text-align: center; padding: 0;'
    },
    handleRemove(file, fileList) {
      console.log(file, fileList)
    },
    handlePreview(file) {
      console.log(file)
    },
    handleExceed(files, fileList) {
      this.$message.warning(
        `当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${
          files.length + fileList.length
        } 个文件`
      )
    },
    beforeRemove(file) {
      return this.$confirm(`确定移除 ${file.name}？`)
    },
    showDetail(row) {
      this.$emit('close', { type: row.name })
    },
    updateDialogSize() {
      this.dialogWidth = `${window.innerWidth * 0.6}px` // 宽度为窗口宽度的60%
      this.dialogTop = `${window.innerHeight * 0.1}px` // 顶部为窗口高度的10%
    },
    highlightCode(code) {
      return hljs.highlightAuto(code).value
    },
  },
  watch: {
    // 当选中的详情数据更新时，重新计算分页数据
    selectedDetail() {
      this.currentPage = 1
      this.updatePagination()
    },
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateDialogSize) // 销毁时移除事件监听
  },
}
</script>

<style scoped>
.category {
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

.left {
  width: 32%;
  height: 96%;
  margin: 0% 1%;
  border: thin solid white;
  display: inline-block;
}

.right {
  width: 70%;
  height: 96%;
  display: flex;
}

.left-half {
  height: 50%;
  width: 100%;
  margin-top: 1%;
  display: flex;
}

.right-table {
  width: 97%;
  height: 100%;
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

.full-screen-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  background: rgba(0, 0, 0, 0.8); /* 半透明背景 */
  color: #000000; /* 文字颜色 */
  overflow: auto; /* 当内容过多时支持滚动 */
}

.full-screen-dialog .el-dialog__header {
  background-color: #303133;
  color: #000000;
}

.full-screen-dialog .el-dialog__body {
  padding: 20px;
}

.full-screen-dialog .el-dialog__footer {
  display: none; /* 隐藏默认底部 */
}
.description-cell {
  white-space: nowrap; /* 单行显示 */
  overflow: hidden; /* 隐藏溢出部分 */
  text-overflow: ellipsis; /* 添加省略号 */
}
.adaptive-dialog {
  max-width: 90%;
  max-height: 80%;
  overflow: hidden;
}
.adaptive-dialog .el-dialog__header {
  width: 100%; /* 确保标题栏宽度与弹窗一致 */
  box-sizing: border-box; /* 防止内边距影响宽度 */
}

.adaptive-dialog .el-dialog__body {
  overflow: hidden; /* 避免宽度不同步时出现滚动条 */
  background-color: transparent !important;
}

::v-deep(.el-input__inner){
  height: 35px !important;
  line-height: 35px !important;
  color: black !important;
  font-size: 15px !important;
  background-color: transparent;
  border: 1px solid rgba(13, 14, 14, 0.5);
}

::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active{
  color: black;
}

.code-block {
  background: #282c34; /* 深色背景 */
  color: #000000; /* 高亮文字颜色 */
  padding: 15px; /* 内边距 */
  border-radius: 5px; /* 圆角 */
  overflow: auto; /* 显示滚动条 */
  font-family: 'Courier New', Courier, monospace; /* 等宽字体 */
  font-size: 14px; /* 字体大小 */
  line-height: 1.5; /* 行间距 */
  white-space: pre-wrap; /* 自动换行 */
  word-wrap: break-word; /* 单词断行 */
}

h3 {
  font-size: 16px;
  color: #000000; /* 黑色标题 */
  margin-bottom: 10px;
  text-align: center;
}
</style>
