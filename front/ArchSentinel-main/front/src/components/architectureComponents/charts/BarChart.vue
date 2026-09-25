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
        xAxis: {
          type: 'category',
          data: this.chartData.xAxisItem,
          axisLabel: {
            hideOverlap: true,
            rotate: 30, // If the label names are too long you can manage this by rotating the label.
          },
        },
        yAxis: {
          type: 'value',
        },
        series: [
          {
            data: this.chartData.data,
            type: 'bar',
          },
        ],
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

<style scoped></style>
