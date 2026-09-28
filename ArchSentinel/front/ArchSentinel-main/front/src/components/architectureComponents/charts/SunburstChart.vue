<template>
  <div :id="chartId" :style="chartStyle"></div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  props: ['height', 'width', 'chartData', 'idNumber'],

  data() {
    return {
      chartId: 'bar-chart' + this.idNumber,
      chartStyle: {
        width: this.width,
        height: this.height,
      },
      option: {
        series: {
          type: 'sunburst',
          data: this.chartData, // This will be set during initialization
          radius: [60, '90%'],
          itemStyle: {
            borderRadius: 7,
            borderWidth: 2,
          },
          label: {
            show: true,
          },
        },
      },
    }
  },

  mounted() {
    console.log(this.chartData)
    const chartDom = document.getElementById(this.chartId)
    if (chartDom) {
      const myChart = echarts.init(chartDom)
      this.option && myChart.setOption(this.option)
    }
  },
}
</script>

<style scoped>
/* Optional styles */
</style>
