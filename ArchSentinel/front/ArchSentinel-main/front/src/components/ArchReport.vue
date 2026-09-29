<template>
  <div class="ArchReport">
    <div style="margin-top: 5ch">
      <button class="export-pdf-btn minimal-business" @click="exportToPDF">
        <span class="icon">📄</span>
        <span>导出PDF报告</span>
        <div class="underline"></div>
      </button>
    </div>
    <div class="report-container">
      <section class="report-section">
        <h1 class="title">{{ UpFileInfo.folderName }} vs {{ DownFileInfo.folderName }} 的检测报告</h1>
      </section>
      
      <!-- 第一部分：综合评价 -->
      <ComprehensiveEvaluation />

      <!-- 第二部分：项目分析 -->
      <ProjectAnalysis />

      <!-- 第三部分：建议 -->
      <section class="report-section">
        <h2>总结建议</h2>
        <div class="content-area">
          <div>
            <div>
              <h3 style="text-align: left;">总体情况：</h3>
              <pre style="white-space: pre-wrap; text-align: left;">{{ totalAdvice['总体情况'] }}</pre>
            </div>
            <div>
              <h3 style="text-align: left;">现存问题：</h3>
              <pre style="white-space: pre-wrap; text-align: left;">{{ totalAdvice['现存问题'] }}</pre>
            </div>
            <div>
              <h3 style="text-align: left;">改进建议：</h3>
              <pre style="white-space: pre-wrap; text-align: left;">{{ totalAdvice['改进建议'] }}</pre>
            </div>
        </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import EventBus from './eventBus'
import ComprehensiveEvaluation from './report/ComprehensiveEvaluation.vue'
import ProjectAnalysis from './report/ProjectAnalysis.vue'
import { CHAT_API_KEY } from '../config/runtime'
import { normalizeData } from './report/normalizeData';
import { getJsonResult, resultUrls } from '../services/analysisResults';

export default {
  name: 'ArchReport',
  components: {
    ComprehensiveEvaluation,
    ProjectAnalysis
  },
  data() {
    return {
      UpFileInfo: {
        folderName: '未上传上游项目',
        literallySize: 0,
        fileCount: 0,
        cloc: 0
      },
      DownFileInfo: {
        folderName: '未上传下游项目',
        literallySize: 0,
        fileCount: 0,
        cloc: 0
      },
      API_KEY: CHAT_API_KEY,
      
      totalAdvice: {
        "总体情况": "该项目整体呈现出规模化演进、模块数量与功能日益复杂的趋势。从早期版本到当前版本，代码体量与结构显著增长，体现出较强的业务支撑能力和持续维护意识。然而，随着复杂度增加，项目在模块解耦、编码规范、架构一致性、代码质量及演化策略等方面暴露出较多中高风险问题，尤其是高耦合、低内聚、反模式频发、实体碎片化、侵入式修改泛滥等现象，已对架构稳定性、维护性与可持续演进构成挑战。部分指标（如传播成本上升、解耦性下滑、封装性退化）也显示项目可能正面临系统性技术债累积的风险。",
    
        "现存问题": [
          "1. 模块耦合增强、职责不清：方法和变量增长迅速、关系调用密集，表明系统耦合性高，职责边界模糊，存在“上帝类”“长方法”等设计坏味道风险。",
          "2. 编码规范与质量问题突出：高优先级代码违规频发，涵盖异常处理不当、资源泄露、线程不安全等问题，反映基础工程实践薄弱。",
          "3. 架构反模式严重：如接口未解耦（AWD）、特性依恋（FE）、霰弹式修改（SS）等反模式频发，说明架构设计不合理，变更成本高。",
          "4. 实体碎片化严重，复用机制缺失：actively native 实体占比高达99%，大量重复建模、语义不一致，说明平台能力未形成统一建模与共享标准。",
          "5. 侵入式修改风险高：下游直接修改上游字段、调用链、方法签名等行为广泛存在，缺乏接口契约与扩展边界，严重威胁架构稳定性。",
          "6. 构造性重构不足：虽有一定数量的重构行为，但多集中于方法级微调，缺乏类层级和模块层级的深度结构优化。",
          "7. 指标预警趋势明显：如传播成本（PC）上升、解耦性（DL）下降、封装性（AHF/MHF）恶化、内聚性低等，需警惕潜在架构恶化风险。"
        ],
    
        "改进建议": [
          "1. 推进架构治理与模块重构：采用领域驱动设计（DDD）、边界上下文重构、大模块拆分等方式，优化模块职责与依赖，提升内聚性、降低耦合度。",
          "2. 建立接口契约与扩展机制：明确对外暴露接口边界，推行 API 稳定性管理、版本控制与扩展点机制，杜绝下游直接侵入上游实现。",
          "3. 系统治理代码质量与规范：引入静态分析工具（如 SonarQube）作为CI质量网关，重点治理高优先级问题，配套开发团队规范培训与审查制度。",
          "4. 统一建模与实体治理：推动平台统一实体建模与共享机制，识别冗余与同义实体，构建实体复用库，提升模型一致性与协同效率。",
          "5. 控制侵入式修改风险：设立CI闸口检查侵入性改动，结合契约测试、回归测试机制，确保接口变更可控、兼容。",
          "6. 强化结构性重构：推动类级别重构与架构层面优化，如继承结构清理、职责分离、重构大类为多个职责单一的小类，提升系统演化能力。",
          "7. 建立架构度量与监控机制：持续追踪传播成本、耦合面、内聚性、封装性等指标变化，引入结构演进仪表盘与预警系统，形成主动治理能力。"
        ]
      },

    }
  },
  mounted() {
    this.totalAdvice = normalizeData(this.totalAdvice);
    
    this.UpFileInfo = EventBus.UpFileInfo || this.UpFileInfo
    this.DownFileInfo = EventBus.DownFileInfo || this.DownFileInfo
    this.pmdList = EventBus.pmdList
    this.antiPatternList = EventBus.antiPatternList
  },
  methods: {
    async exportToPDF() {
      const reportContainer = document.querySelector('.report-container')

      // 设置缩放比例提升清晰度
      const scale = 2
      const options = {
        scale,
        useCORS: true, // 允许跨域图片
        logging: true,
        backgroundColor: '#ffffff', // 强制白色背景
      }

      // 分页截取内容
      const pdf = new jsPDF('p', 'mm', 'a4')
      const pageHeight = pdf.internal.pageSize.getHeight()

      try {
        const canvas = await html2canvas(reportContainer, options)
        const imgData = canvas.toDataURL('image/png', 1.0)
        const imgWidth = 190 // A4纸张宽度（210mm）减去边距
        const imgHeight = (canvas.height * imgWidth) / canvas.width

        let heightLeft = imgHeight
        let position = 0

        // 添加首页
        pdf.addImage(imgData, 'PNG', 10, position + 10, imgWidth, imgHeight)
        heightLeft -= pageHeight

        // 添加后续分页
        while (heightLeft > 0) {
          position = heightLeft - imgHeight
          pdf.addPage()
          pdf.addImage(imgData, 'PNG', 10, position + 10, imgWidth, imgHeight)
          heightLeft -= pageHeight
        }

        pdf.save('架构分析报告.pdf')
      } catch (error) {
        console.error('导出失败:', error)
        alert('导出失败，请检查控制台日志')
      }
    },
    async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''

        const reportMessage = await getJsonResult(resultUrls.report)
        this.userInput = "你现在是软件架构高级工程师，现在我会给你这个项目的各个维度的分析结论，请你依据分析结论给出最终的总结建议。这是各个维度的分析结论：" +
        JSON.stringify(reportMessage) + "。接下来请你对该项目给出最终的总结建议。请按照以下json形式回复我：" +
        " { totalAdvice : {总体情况：、现存问题：、改进建议} }."

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
          this.totalAdvice = this.response.totalAdvice
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
.ArchReport {
  height: 100vh;
  width: 100%;
  overflow-y: auto;
  background-color: #f0f0f0;
}

.report-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  color: black;
}

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

.content-area {
  padding: 16px;
  color: #000000;
  line-height: 1.6;
  font-size: 16px;
}

.content-area p {
  margin-bottom: 12px;
  text-align: left;
  text-indent: 2em;
}

.content-area p:last-child {
  margin-bottom: 0;
}

.tables-container {
  display: flex;
  gap: 24px;
  margin-top: 20px;
}

.table-wrapper {
  flex: 1;
}

.table-wrapper h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #1a1a1a;
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e6e6e6;
}

th {
  background-color: #f5f5f5;
  font-weight: 600;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background-color: #e8e8e8;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
}

.bar-chart {
  width: 100%;
  height: 400px;
  background: #ffffff;
  border-radius: 8px;
  padding: 16px;
}

.pie-charts-container {
  display: flex;
  gap: 24px;
  margin: 20px 0;
}

.pie-chart {
  flex: 1;
  height: 400px;
  background: #ffffff;
  border-radius: 8px;
  padding: 16px;
}

/* 为了更好地显示长表格，添加以下样式 */
.table-wrapper {
  margin: 20px 0;
  overflow-x: auto;
}

table {
  min-width: 800px;
  /* 确保表格有最小宽度 */
}

td {
  white-space: normal;
  /* 允许文字换行 */
  max-width: 300px;
  /* 限制单元格最大宽度 */
}

.export-pdf-btn.minimal-business {
  padding: 12px 24px;
  background: transparent;
  border: 2px solid #2c3e50;
  border-radius: 0;
  color: #2c3e50;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
  position: relative;
}

.underline {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #2c3e50;
  transition: width 0.3s;
}

.minimal-business:hover {
  background: rgba(44, 62, 80, 0.05);
}

.minimal-business:hover .underline {
  width: 100%;
}

.icon {
  margin-right: 8px;
  transition: opacity 0.3s;
}

.minimal-business:hover .icon {
  opacity: 0.8;
}
</style>
