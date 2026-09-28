<template>
  <div
    class="IntrusiveModify"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="top-table">
        <el-table
          :data="showAllType"
          highlight-current-row
          height="99%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column prop="name" label="类型" />
          <el-table-column prop="content" label="解释" />
          <el-table-column prop="count" label="条数" width="100" sortable />
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <button class="btn" @click="showSpecificType(scope.row)">
                查看详情
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="down-table">
        <el-table
          :data="showOneType"
          highlight-current-row
          height="90%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column
            label="实体ID"
            prop="id"
            width="200"
          ></el-table-column>
          <el-table-column
            label="category"
            prop="category"
            width="200"
          ></el-table-column>
          <el-table-column
            label="qualifiedName"
            prop="qualifiedName"
            width="200"
          ></el-table-column>
          <el-table-column label="filePath" prop="file_path"></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <button class="btn" @click="showDetail(scope.row)">
                查看详情
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog
      title="上下游代码对比"
      :visible.sync="showCodeDialog"
      width="80%"
      append-to-body
      header-align="center"
    >
      <div class="code-container">
        <!-- 上游代码 -->
        <div class="code-section" style="background-color: #ababab">
          <h3>上游代码</h3>
          <pre><code class="language-java" ref="upstreamCode" v-html="upstreamCode"></code></pre>
        </div>
        <!-- 下游代码 -->
        <div class="code-section" style="background-color: #ababab">
          <h3>下游代码</h3>
          <pre><code class="language-java" ref="downstreamCode" v-html="downstreamCode"></code></pre>
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
import axios from 'axios'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import filepath from '../../../public/filepaths.json'
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
      dataLoading: false,
      localParentData: { ...this.parentData },
      fileContent: [],
      showEnerGap: false,
      fileList: [],

      upstreamFilePath: '',
      downstreamFilePath: '',
      upstreamFileEnrePath: '',
      downstreamFileEnrePath: '',

      intrusiveTypeCounts: {},

      showCodeDialog: false,
      upstreamCode: '',
      downstreamCode: '',

      showAllType: [],
      showOneType: [],
      targetFileExtensions: [
        'class_access_modify_entities.csv',
        'method_access_modify_entities.csv',
        'variable_access_modify_entities.csv',
        'inner_extensive_class_entities.csv',
        'class_var_extensive_entities.csv',
        'class_var_remove_entities.csv',
        'class_annotation_modify_entities.csv',
        'method_annotation_modify_entities.csv',
        'variable_annotation_modify_entities.csv',
        'parent_interface_modify_entities.csv',
        'parent_class_modify_entities.csv',
        'inner_extensive_interface_entities.csv',
        'inner_remove_interface_entities.csv',
        'method_call_extensive_entities.csv',
        'method_call_remove_entities.csv',
        'param_add_entities.csv',
        'param_modify_entities.csv',
        'param_remove_entities.csv',
        'class_var_modify_entities.csv',
        'method_var_modify_entities.csv',
      ],
    }
  },
  mounted() {
    this.showEnerGap = true
    this.loadIntrusiveFiles()
  },
  methods: {
    async loadIntrusiveFiles() {
      this.dataLoading = true
      this.showAllType = []

      try {
        // 直接使用targetFileExtensions中的文件列表
        for (const file of this.targetFileExtensions) {
          await this.handleIntrusiveFile(file)
        }
      } catch (error) {
        console.error('加载文件失败:', error)
        this.$message.error('加载文件失败！')
      } finally {
        this.dataLoading = false
        EventBus.intrusiveList = this.showAllType
        EventBus.intrusiveTypeCounts = this.intrusiveTypeCounts
      }
    },
    async handleIntrusiveFile(file) {
      try {
        const response = await fetch(`/intrusive_analysis/${file}`)
        const csvContent = await response.text()

        const parsedData = Papa.parse(csvContent, {
          header: true,
          skipEmptyLines: true,
        })

        this.showAllType.push({
          name: this.getTypeByName(file.split('.').slice(0, -1).join('.')),
          content: this.getContentByName(
            file.split('.').slice(0, -1).join('.')
          ),
          count: parsedData.data.length,
          data: parsedData.data,
        })
        const type = this.getTypeByName(file.split('.').slice(0, -1).join('.'))
        const count = parsedData.data.length
        this.intrusiveTypeCounts[type] = (this.intrusiveTypeCounts[type] || 0) + count
        
      } catch (error) {
        console.error('处理文件失败:', error)
      }
    },
    getTypeByName(type) {
      const typeDescriptions = {
        class_access_modify_entities: 'Modify Class Accessibility',
        method_access_modify_entities: 'Modify Method Accessibility',
        variable_access_modify_entities: 'Modify Variable Accessibility',
        inner_extensive_class_entities: 'Newly defined Inner Class',
        class_var_extensive_entities: 'Add Class Field',
        class_var_remove_entities: 'Remove Class Field',
        class_annotation_modify_entities: 'Modify Annotation on Class Entity',
        method_annotation_modify_entities: 'Modify Annotation on Method Entity',
        variable_annotation_modify_entities:
          'Modify Annotation on Variable Entity',
        parent_interface_modify_entities:
          'Native Class Implements Extensive Interface',
        parent_class_modify_entities:
          'Native Class Inherits Extensive Base-Class',
        inner_extensive_interface_entities: 'Newly Defined Inner Interface',
        inner_remove_interface_entities: 'Remove Inner Interface',
        method_call_extensive_entities: 'Add Call Code',
        method_call_remove_entities: 'Remove Call Code',
        param_add_entities: 'Add Parameters of Method',
        param_modify_entities: 'Modify Parameters of Method',
        param_remove_entities: 'Remove Parameters of Method',
        class_var_modify_entities: 'Modify Class Field Value',
        method_var_modify_entities: 'Modify Attribute in Method',
      }
      return typeDescriptions[type] || '未知类型'
    },
    getContentByName(type) {
      const typeDescriptions = {
        class_access_modify_entities: '原生类的访问权限被修改',
        method_access_modify_entities: '原生方法的访问权限被修改',
        variable_access_modify_entities: '原生变量的访问权限被修改',
        inner_extensive_class_entities: '原生类添加伴生内部类',
        class_var_extensive_entities: '原生类中添加字段',
        class_var_remove_entities: '原生类字段的删除',
        class_annotation_modify_entities: '原生类实体的注解修改',
        method_annotation_modify_entities: '原生方法实体的注解修改',
        variable_annotation_modify_entities: '原生变量实体的注解修改',
        parent_interface_modify_entities: '原生类实现扩展接口',
        parent_class_modify_entities: '原生类继承伴生类',
        inner_extensive_interface_entities: '原生类增加内部接口',
        inner_remove_interface_entities: '原生类删除内部接口',
        method_call_extensive_entities: '原生方法内调用语句的新增',
        method_call_remove_entities: '原生方法内调用语句的删除',
        param_add_entities: '原生方法的参数增加',
        param_modify_entities: '原生方法的参数修改',
        param_remove_entities: '原生方法的参数删除',
        class_var_modify_entities: '原生类字段赋值语句修改',
        method_var_modify_entities: '原生方法内变量赋值语句修改',
      }
      return typeDescriptions[type] || '未知类型'
    },
    isTargetFile(fileName) {
      return (
        this.targetFileExtensions.find((file) => fileName.includes(file)) ||
        null
      )
    },
    async showSpecificType(row) {
      this.dataLoading = true
      if (row.count > 500) {
        await new Promise((resolve) => setTimeout(resolve, 100))
      }
      this.showOneType = row.data
      this.dataLoading = false
    },
    async showDetail(row) {
      this.upstreamCode = ''
      this.downstreamCode = ''

      this.dataLoading = true
      this.upstreamFilePath = decodeURIComponent(filepath.upstreamFilePath)
      this.downstreamFilePath = decodeURIComponent(filepath.downstreamFilePath)
      this.upstreamFileEnrePath = decodeURIComponent(
        filepath.upstreamFileEnrePath
      )
      this.downstreamFileEnrePath = decodeURIComponent(
        filepath.downstreamFileEnrePath
      )

      try {
        // 调用后端接口获取上下游代码
        const response = await axios.post(
          '/api/getIntrusiveCode',
          {
            upstreamProjectPath: this.upstreamFilePath,
            downstreamProjectPath: this.downstreamFilePath,
            upstreamDependencyPath: this.upstreamFileEnrePath,
            downstreamDependencyPath: this.downstreamFileEnrePath,
            entity: row, // 实体信息
          }
        )

        const data = response.data

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
        this.$message.error(`获取代码片段失败：${message}`)
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
  },
}
</script>

<style scoped>
.IntrusiveModify {
  width: 100%;
  height: 100%;
  display: inline-block;
  background-color: #f0f0f0;
}

.main {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 1%;
}

.top-table {
  width: 100%;
  height: 50%;
  margin-bottom: 1%;
}

.down-table {
  width: 100%;
  height: 50%;
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
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.code-section {
  width: 50%;
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
  color: #606266;
}
</style>
