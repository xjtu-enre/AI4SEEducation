<template>
    <div class="barChart" ref="barChart"></div>
</template>

<script>
import * as echarts from 'echarts'

export default {
    data(){
        return{
            barChart: null,
        }
    },
    mounted(){
        this.barChart = echarts.init(this.$refs.barChart)
    },
    methods:{
        initBarChart(barData) {
            const barOption = {
                toolbox: {
                    itemSize: 20,
                    iconStyle: {
                        borderColor: '#000',
                    },
                    feature: {
                        dataView: {},
                        // magicType:{
                        //     type:['line','bar','stack','tiled'],
                        // },
                        restore: {},
                        saveAsImage: {},
                    },
                },
                // title: {
                //     text: barData.title,
                //     left: 'center',
                //     top: 'top',
                //     textStyle: {
                //         color: '#000000',
                //         fontSize: '1.1rem',
                //     },
                // },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'shadow' }
                },
                xAxis: {
                    type: 'value',
                    name: '数量'
                },
                yAxis: {
                    type: 'category',
                    name: '类型',
                    data: barData.category
                },
                grid: {
                    left: '5%', // 调整y轴所占的宽度比例
                    containLabel: true, // 确保标签包含在绘图区域内
                },
                series: [
                    {
                        type: 'bar',
                        data: barData.data
                    },
                ],
            }
            this.barChart.setOption(barOption)
        },
    },
    beforeDestroy() {
        if (this.barChart) {
            this.barChart.dispose()
        }
    },
}
</script>

<style scoped>

</style>