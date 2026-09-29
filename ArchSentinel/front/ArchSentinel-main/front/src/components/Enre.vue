<template>
    <div class="Enre">
        <div class="left"
            v-loading.lock="UpFilesDataLoading"
            element-loading-text="数据加载中..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.3)"
        >
            <div class="upload-section">
                <span>原始版本项目文件夹：</span>
                <input
                    type="file"
                    webkitdirectory 
                    multiple
                    class="hidden-intrusive1"
                    @change.prevent="handleUpFiles"
                />
                <button
                    class="btn"
                    @click="triggerIntrusive1"
                >
                    选择文件夹
                </button>
            </div>
            <div class="Enre-tabs">
              <ArchitectureLeft ref="childLeft" />
            </div>
            <!-- <div class="Enre-tabs">
              <el-tabs v-model="activeTab" style="margin-top: 1%; color: black">
                <el-tab-pane label="整体项目概览" name="ener">
                  <Architecture />
                </el-tab-pane>
              </el-tabs>
            </div> -->
        </div>
        <div class="right"
            v-loading.lock="DownFilesDataLoading"
            element-loading-text="数据加载中..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.3)"
        >
            <div class="upload-section">
                <span>最终版本项目文件夹：</span>
                <input
                    type="file"
                    webkitdirectory
                    multiple
                    class="hidden-intrusive2"
                    @change.prevent="handleDownFiles"
                />
                <button
                    class="btn"
                    @click="triggerIntrusive2"
                >
                    选择文件夹
                </button>
            </div>
            <div class="Enre-tabs">
              <ArchitectureRight ref="childRight" />
            </div>
        </div>
    </div>
  </template>
  
<script>
import axios from 'axios'
import ArchitectureLeft from './tabs/DependencyGraphLeft.vue'
import ArchitectureRight from './tabs/DependencyGraphRight.vue'
import EventBus from './eventBus';
  
export default {
    name: 'TargetComponent',
    components: {
        ArchitectureLeft,
        ArchitectureRight
    },
    data() {
      return {
        UpFilesDataLoading: false,
        UpFileInfo: {
            folderName: null, // 提取的文件夹名
            totalSize: null,
            literallySize: null,
            files: [],
        },
        DownFilesDataLoading: false,
        DownFileInfo: {
            folderName: null, // 提取的文件夹名
            totalSize: null,
            literallySize: null,
            files: [],
        },
        upstreamFilePath: '', // 上游项目文件路径
        downstreamFilePath: '', // 下游项目文件路径
        upstreamFileEnrePath: '', // 上游项目Enre分析文件路径
        downstreamFileEnrePath: '', // 下游项目Enre分析文件路径
  

        treeData: [], // 存储文件夹树形结构
        defaultProps: {
            label: 'label',
            children: 'children'
        },

        isCompleted: false, // 是否生成完毕
        activeTab: 'ener', // 当前活动的标签
      }
    },
    methods: {
        handleFilesInfo(files){
            let totalSize = 0;
            const fileDetails = [];
            let folderName = null;

            // 遍历文件夹中的所有文件
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                
                // 获取文件的相对路径
                const relativePath = file.webkitRelativePath;

                // 从路径中提取文件夹名称（路径的第一部分）
                if (!folderName) {
                    // 提取文件夹名（去掉文件名部分，只保留文件夹路径）
                    folderName = relativePath.split('/')[0];
                }

                // 添加文件信息
                fileDetails.push({
                    name: file.name,
                    size: file.size,
                });
                totalSize += file.size; // 累加文件大小
            }

            let literallySize = 0
            if (totalSize >= 1024 * 1024) {
                literallySize =  (totalSize / (1024 * 1024)).toFixed(2) + " MB"
            } else if (totalSize >= 1024) {
                literallySize = (totalSize / 1024).toFixed(2) + " KB";
            } else {
                literallySize = totalSize + " Bytes";
            }

            // 更新文件夹信息
            let info = {
                folderName: folderName, // 提取的文件夹名
                totalSize: totalSize,
                literallySize: literallySize,
                fileCount: files.length,
                cloc: null,
                files: files
            };

            return info;
        },
        updateLanguageData() {
            const files = [
                ...(this.UpFileInfo.files || []),
                ...(this.DownFileInfo.files || [])
            ]
            const languageByExtension = {
                java: 'Java', js: 'JavaScript', ts: 'TypeScript', py: 'Python',
                c: 'C', h: 'C/C++ Header', cpp: 'C++', cc: 'C++', cs: 'C#',
                go: 'Go', rs: 'Rust', kt: 'Kotlin', scala: 'Scala', rb: 'Ruby', php: 'PHP'
            }
            const counts = {}
            files.forEach(file => {
                const extension = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
                const language = languageByExtension[extension] || 'Other'
                counts[language] = (counts[language] || 0) + 1
            })
            EventBus.languageData = Object.entries(counts).map(([name, value]) => ({ name, value }))
        },
        async handleUpFiles(event) {
            this.UpFilesDataLoading = true

            const files = event.target.files; // 获取选中的文件列表

            this.UpFileInfo = this.handleFilesInfo(files)
            EventBus.UpFileInfo = this.UpFileInfo
            this.updateLanguageData()
            this.$refs.childLeft.handleFileInfo(this.UpFileInfo)

            event.preventDefault() // 阻止默认表单提交行为
            try {
                const files = event.target.files
                const formData = new FormData()
        
                for (let i = 0; i < files.length; i++) {
                    formData.append('files', files[i])
                }
        
                const response = await axios.post(
                    '/api/processUpFiles',
                    formData,
                    {
                        headers: {
                        'Content-Type': 'multipart/form-data',
                        },
                    }
                )
        
                this.upstreamFilePath = response.data.UpFilesDirPath
                this.upstreamFileEnrePath = response.data.UpFilesEnreDirPath
                EventBus.upstreamFilePath = this.upstreamFilePath
                EventBus.upstreamFileEnrePath = this.upstreamFileEnrePath
                await this.$refs.childLeft.getEntity()
                this.$message.success(`上游文件处理成功！`)
                // this.sendDataToParent()
            } catch (error) {
                console.error('上游文件处理失败:', error)
                this.$message.error('上游文件处理失败，请检查后端工具日志')
            } finally {
                this.UpFilesDataLoading = false
            }
        },
  
        async handleDownFiles(event) {
            this.DownFilesDataLoading = true

            const files = event.target.files; // 获取选中的文件列表

            this.DownFileInfo = this.handleFilesInfo(files)
            EventBus.DownFileInfo = this.DownFileInfo
            this.updateLanguageData()
            console.log('DownFileInfo', this.DownFileInfo)
            this.$refs.childRight.handleFileInfo(this.DownFileInfo)

            event.preventDefault() // 阻止默认表单提交行为
            try {
                const files = event.target.files
                const formData = new FormData()
        
                for (let i = 0; i < files.length; i++) {
                    formData.append('files', files[i])
                }
        
                const response = await axios.post(
                    '/api/processDownFiles',
                    formData,
                    {
                        headers: {
                        'Content-Type': 'multipart/form-data',
                        },
                    }
                )
        
                this.downstreamFilePath = response.data.DownFilesDirPath
                this.downstreamFileEnrePath = response.data.DownFilesEnreDirPath
                EventBus.downstreamFilePath = this.downstreamFilePath
                EventBus.downstreamFileEnrePath = this.downstreamFileEnrePath
                await this.$refs.childRight.getEntity()
                this.$message.success(`下游文件处理成功！`)
                // this.sendDataToParent()
            } catch (error) {
                console.error('下游文件处理失败:', error)
                this.$message.error('下游文件处理失败，请检查后端工具日志')
            }  finally {
                this.DownFilesDataLoading = false
            }
        },
    
        triggerIntrusive1(event) {
            event.stopPropagation()
            this.$el.querySelector('.hidden-intrusive1').click()
        },
    
        triggerIntrusive2(event) {
            event.stopPropagation()
            this.$el.querySelector('.hidden-intrusive2').click()
        },
    
        sendDataToParent() {
            const paths = {
                upstreamFilePath: this.upstreamFilePath,
                downstreamFilePath: this.downstreamFilePath,
                upstreamFileEnrePath: this.upstreamFileEnrePath,
                downstreamFileEnrePath: this.downstreamFileEnrePath,
            }
        
            this.$emit('updateData', paths)
        },
    },
}
</script>
  
<style scoped>
.Enre {
    height: 100vh;
    width: 100%;
    display: flex;
}
  
.left {
    width: 50%;
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #f0f0f0;
    margin-right: 0.2%;
    padding-top: 0.5%;
}

.right {
    width: 50%;
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #f0f0f0;
    padding-top: 0.5%;
}
  
.upload-section {
    height: 5%;
    width: 100%;
    justify-content: space-around;
}
  
.upload-section span {
    font-size: 1.25rem;
    color: black;
}

.Enre-tabs {
    height: 90%;
    width: 99%;
}
  
.hidden-intrusive1,
.hidden-intrusive2 {
    display: none;
}
  
::v-deep .el-tabs__header {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}
  
::v-deep .el-tabs__nav-wrap {
    display: flex;
    justify-content: center;
}
  
::v-deep .el-tabs__nav {
    float: none;
}
  
::v-deep .el-tabs__item {
    font-size: 24px;
    padding: 0 50px;
}
</style>
  
