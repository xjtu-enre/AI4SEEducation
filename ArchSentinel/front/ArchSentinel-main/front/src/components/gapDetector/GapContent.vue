<template>
  <div class="GapContent"
    v-loading.fullscreen.lock="dataLoading"
    element-loading-text="数据加载中..."
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.3)"
  >
    <div class="main">
      <div class="main-table">
        <el-table
          :data="antiPatternList"
          highlight-current-row
          height="100%"
          :cell-style="getCellStyle"
          :header-cell-style="getHeaderStyle"
        >
          <el-table-column prop="name" label="名称" width="100" />
          <el-table-column prop="fullName" label="全称" />
          <el-table-column prop="chineseName" label="中文" width="200" />
          <el-table-column prop="content" label="解释" />
          <el-table-column prop="total" label="总数" width="150" sortable/>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <button class="btn" @click="showDetail(scope.row)">
                查看详情
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="main-pieChart">
        <div ref="pieChart" style="width: 97%; height: 100%"></div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import EventBus from '../eventBus'

export default {
  data() {
    return {
      dataLoading: false,
      total: {
        'AWD': 0,
        'CD': 0,
        'CH': 0,
        'DC': 0,
        'FE': 0,
        'MH': 0,
        'SS': 0,
      },
      antiPatternList: [
        {
          name: 'AWD',
          fullName: 'Abstraction Without Decoupling',
          chineseName: '接口未解耦',
          content: '一个类同时依赖于抽象类与其实现类',
          total: 0,
        },
        {
          name: 'CD',
          fullName: 'Cyclic Dependency',
          chineseName: '循环依赖',
          content: '两个或多个模块之间相互直接或间接依赖',
          total: 0,
        },
        {
          name: 'CH',
          fullName: 'Cyclic Hierarchy',
          chineseName: '循环继承',
          content: '父类直接或间接依赖于它的一个子类',
          total: 0,
        },
        {
          name: 'DC',
          fullName: 'Data Clumps',
          chineseName: '数据泥团',
          content: '一组经常一起出现的数据项在多个地方重复出现',
          total: 0,
        },
        {
          name: 'FE',
          fullName: 'Feature Envy',
          chineseName: '特性依恋',
          content: '一个方法过度依赖于其他类中的方法而非其所属类中的方法',
          total: 0,
        },
        {
          name: 'MH',
          fullName: 'Multipath Hierarchy',
          chineseName: '多重路径继承',
          content: '子类到其父类存在多条继承路径',
          total: 0,
        },
        {
          name: 'SS',
          fullName: 'Shotgun Surgery',
          chineseName: '霰弹式修改',
          content: '当系统每遇到某种变化，都必须在许多不同的类内做出许多小修改',
          total: 0,
        },
      ],

      pieChart: null,
    }
  },
  mounted(){
    this.dataLoading = true
    this.updateTotalValues();
  },
  methods: {
    updateTotalValues() {
      const files = ['AWD', 'CD', 'CH', 'DC', 'FE', 'MH', 'SS'];
      
      // 创建一个用于保存所有请求的 Promise 数组
      const requests = files.map((file) => {
        return axios.get(`/api/results/gap/${file}`)
          .then((res) => {
            // 更新 total 对应的值
            this.total[file] = res.data.count;
            if (file === 'SS') {
              this.total[file] = res.data.count_ShotgunSurgery;
            }

            // 更新 antiPatternList 对应的 total 值
            const showItem = this.antiPatternList.find((item) => item.name === file);
            if (showItem) {
              showItem.total = this.total[file];
            }
          })
          .catch((error) => {
            console.error(`加载后端分析结果 ${file} 时出错:`, error);
            return Promise.resolve();  // 确保即使请求失败也返回已解决的 Promise
          })
          .finally(()=>{
            this.dataLoading = false;
            EventBus.antiPatternList = this.antiPatternList;
          })
      });

      // 等待所有请求完成，不管是否成功
      Promise.all(requests)
        .then(() => {
          // 所有请求完成后，更新饼图
          this.initPieChart(this.antiPatternList);
        })
        .catch((error) => {
          console.error('加载数据时出错:', error);
        });
    },
    getHeaderStyle() {
      return {
        backgroundColor: 'transparent',
        color: '#000', // 设置表头文字为黑色
        textAlign: 'center',
        fontWeight: 'bold', // 表头加粗
      }
    },
    getCellStyle() {
      return {
        color: '#000', // 设置单元格文字为黑色
        textAlign: 'center',
        border: 'none', // 去掉单元格边框
      }
    },
    showDetail(row) {
      this.$emit('close', { type: row.name })
      console.log(row)
    },
    initPieChart(showListData) {
      this.pieChart = echarts.init(this.$refs.pieChart)

      const pieData = showListData.map(item => ({
        value: Number(item.total), // 全是0
        name: item.name,
      }));

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
          text: '反模式检测结果分析',
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
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              textStyle: {
                color: '#000000',
                fontSize: '1.25rem',
              },
            },
            data: pieData,
          },
        ],
      }
      this.pieChart.setOption(pieOption)
    },
  },
  beforeDestroy() {
    if (this.pieChart) {
      this.pieChart.dispose()
    }
  },
}
</script>

<style scoped>
.GapContent {
  width: 100%;
  height: 100%;
  /* display: flex; */
  background-color: #f0f0f0;
  /* flex-direction: column; 调整为纵向布局 */
}

.main {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.main-table {
  height: 54%;
  width: 98%;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  margin: 1% 0 0 1%;
}

.main-pieChart {
  height: 49%;
  width: 100%;
  margin-top: 1%;
}
</style>
