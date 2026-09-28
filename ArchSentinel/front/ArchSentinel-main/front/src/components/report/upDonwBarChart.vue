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
            const upData = barData.upData;
            const downData = barData.downData;

            // 提取 name 列表作为 y 轴（保证顺序一致）
            const names = upData.map(item => item.name);

            // 提取 value
            const upValues = upData.map(item => item.value);
            const downValues = downData.map(item => {
                const match = upData.find(up => up.name === item.name);
                return match ? item.value : 0; // 确保顺序一致
            });

            // 计算差值（下游 - 上游）
            const diffValues = names.map((name, index) => {
            const up = upData.find(item => item.name === name)?.value || 0;
            const down = downData.find(item => item.name === name)?.value || 0;
                return down - up;
            });

            const barOption = {
                toolbox: {
                    itemSize: 20,
                    iconStyle: {
                        borderColor: '#000',
                    },
                    feature: {
                        dataView: {},
                        magicType:{
                            type:['line','bar','stack','tiled'],
                        },
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
                legend: {
                    bottom: 30,
                    data: ['上游项目', '下游项目', '差值']
                },
                yAxis: {
                    type: 'value',
                    name: '数量'
                },
                xAxis: {
                    type: 'category',
                    name: '类型',
                    data: names
                },
                series: [
                    {
                        name: '上游项目',
                        type: 'bar',
                        data: upValues,
                        itemStyle: { color: '#5470C6' }
                    },
                    {
                        name: '下游项目',
                        type: 'bar',
                        data: downValues,
                        itemStyle: { color: '#91CC75' }
                    },
                    {
                        name: '差值',
                        type: 'bar',
                        data: diffValues,
                        itemStyle: { color: '#EE6666' }
                    }
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