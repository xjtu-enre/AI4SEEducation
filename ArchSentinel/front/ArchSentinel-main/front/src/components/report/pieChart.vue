<template>
    <div class="pieChart" ref="pieChart"></div>
</template>

<script>
import * as echarts from 'echarts'

export default {
    data(){
        return{
            pieChart: null,
        }
    },
    mounted(){
        this.pieChart = echarts.init(this.$refs.pieChart)
    },
    methods:{
        initPieChart(pieData) {
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
                tooltip: {
                    trigger: 'item',
                    formatter: '{b}: {c} ({d}%)',
                    textStyle: {
                        fontSize: 18,
                    },
                },
                legend: {
                    top: 'bottom',
                    show: !pieData.hasOwnProperty('legend'),
                },
                series: [
                    {
                        type: 'pie',
                        radius: ['40%', '70%'],
                        avoidLabelOverlap: false,
                        label: {
                            formatter: '{b}: {d}%',
                            textStyle: {
                                color: '#000000',
                                fontSize: '1rem',
                            },
                        },
                        data: pieData.data,
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

</style>