<template>
  <div :id="chartId" :style="chartStyle"></div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  props: ['height', 'width', 'chartData', 'idNumber'],

  data() {
    return {
      chartId: 'doughnut-chart' + this.idNumber,
      chartStyle: {
        width: this.width,
        height: this.height,
      },
      option: {
        tooltip: {
          trigger: 'item',
        },
        legend: {
          top: '5%',
          left: 'center',
        },
        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '70%'],
            startAngle: 180,
            endAngle: 360,
            data: this.chartData,
          },
        ],
      },
    }
  },

  mounted() {
    const chartDom = document.getElementById(this.chartId)
    if (chartDom) {
      const myChart = echarts.init(chartDom)
      this.option && myChart.setOption(this.option)
    }
  },
}
</script>

<style scoped></style>
