<template>
  <div
    class="DC"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)">

    <div class="main" v-show="fileFind">找到文件</div>

    <div class="main" v-show="!fileFind" style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60vh;">
      <i class="el-icon-document-delete" style="color: black; font-size: 260px;"></i>
      <span style="color: black; margin-top: 10px; font-size: 28px">找不到 DC 文件</span>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "DC",
  data() {
    return {
      fileFind: true,
      dataLoading: false,

      jsonData: null,
    }
  },
  mounted() {
    this.dataLoading = true;
    this.loadDataAndInitChart();
  },
  methods: {
    loadDataAndInitChart() {
      axios
        .get("/DownFiles-DC.json")
        .then((res) => {
          this.jsonData = res.data.instances;
          this.dataLoading = false
        })
        .catch((error) => {
          console.error("Error loading data:", error);
          if (error.status === 404) {
            this.fileFind = false
            this.dataLoading = false
            this.$message({
              message: '未生成 DC 文件',
              type: 'warning'
            })
            return
          }
        });
    }
  }
}
</script>

<style scoped>
.DC {
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
</style>