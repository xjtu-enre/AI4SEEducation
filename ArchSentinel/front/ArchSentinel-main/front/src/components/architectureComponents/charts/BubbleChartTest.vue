<template>
    <div class="bubble-chart-container" :style="{ width: width, height: height }">
      <div ref="chartContainer" class="bubble-chart"></div>
      <div ref="tooltip" class="tooltip"></div>
    </div>
  </template>
  
  <script>
  import * as d3 from "d3";
  
  export default {
    name: "BubbleChart",
    data() {
      return {
        chartData: [
          { name: "Bubble 1", value: 18 },
          { name: "Bubble 2", value: 19 },
          { name: "Bubble 3", value: 24 },
          { name: "Bubble 4", value: 37 },
          { name: "Bubble 5", value: 21 },
        ],
        width: "220px", // 画布宽度
        height: "220px", // 画布高度
      };
    },
    methods: {
      drawChart() {
        const svgWidth = parseInt(this.width);
        const svgHeight = parseInt(this.height);
        const data = this.chartData;
  
        // 清空之前的图表
        d3.select(this.$refs.chartContainer).selectAll("*").remove();
  
        // 创建 SVG 容器
        const svg = d3
          .select(this.$refs.chartContainer)
          .append("svg")
          .attr("width", svgWidth)
          .attr("height", svgHeight)
          .attr("viewBox", `0 0 ${svgWidth} ${svgHeight}`)
          .attr("preserveAspectRatio", "xMidYMid meet");
  
        // 提示框元素
        const tooltip = d3.select(this.$refs.tooltip);
  
        // 颜色比例尺
        const colorScale = d3
          .scaleOrdinal()
          .domain(data.map((d) => d.name))
          .range(d3.schemeCategory10);
  
        // 模拟力导向布局
        const simulation = d3
          .forceSimulation(data)
          .force(
            "center",
            d3.forceCenter(svgWidth / 2, svgHeight / 2) // 中心力
          )
          .force(
            "charge",
            d3.forceManyBody().strength(5) // 排斥力
          )
          .force(
            "collision",
            d3.forceCollide().radius((d) => d.value + 5) // 碰撞检测，防止重叠
          )
          .on("tick", ticked);
  
        // 绘制泡泡容器
        const bubbles = svg
          .selectAll("g")
          .data(data)
          .enter()
          .append("g")
          .attr("cursor", "pointer");
  
        // 绘制泡泡
        bubbles
          .append("circle")
          .attr("r", (d) => d.value) // 半径根据数据大小
          .attr("fill", (d) => colorScale(d.name)) // 设置颜色
          .attr("opacity", 0.8)
          .on("mouseover", function (event, d) {
            // 显示提示框
            tooltip
              .style("visibility", "visible")
              .html(
                `<strong>${d.name}</strong><br>Value: ${d.value}`
              );
            d3.select(this).attr("opacity", 1); // 高亮当前泡泡
          })
          .on("mousemove", function (event) {
            // 跟随鼠标
            tooltip
              .style("top", `${event.pageY + 10}px`)
              .style("left", `${event.pageX + 10}px`);
          })
          .on("mouseout", function () {
            // 隐藏提示框
            tooltip.style("visibility", "hidden");
            d3.select(this).attr("opacity", 0.8); // 恢复透明度
          });
  
        // 绘制泡泡中的数字
        bubbles
          .append("text")
          .attr("text-anchor", "middle")
          .attr("dy", ".3em") // 垂直对齐调整
          .text((d) => d.value) // 显示泡泡值
          .attr("fill", "white")
          .attr("font-size", "15px")
          .attr("pointer-events", "none"); // 防止文本阻挡鼠标事件
  
        // 更新每个泡泡的位置
        function ticked() {
          bubbles.attr("transform", (d) => `translate(${d.x}, ${d.y})`);
        }
      },
    },
    mounted() {
      this.drawChart(); // 组件挂载后绘制图表
    },
  };
  </script>
  
  <style scoped>
  .bubble-chart-container {
    position: relative;
  }
  
  .tooltip {
    position: absolute;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 5px 10px;
    border-radius: 5px;
    font-size: 12px;
    pointer-events: none;
    visibility: hidden;
    z-index: 1000;
  }
  </style>
  