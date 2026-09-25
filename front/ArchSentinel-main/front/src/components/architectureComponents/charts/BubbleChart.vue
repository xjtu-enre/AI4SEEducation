<template>
  <div ref="bubbleChart" class="bubble-chart" :style="containerStyle"></div>
</template>

<script>
import * as d3 from "d3";

export default {
  name: "BubbleChart",
  props: {
    chartData: {
      type: Array,
      default: () => [
        { id: 1, value: 18, color: "#66c2a5" },
        { id: 2, value: 19, color: "#fc8d62" },
        { id: 3, value: 24, color: "#8da0cb" },
        { id: 4, value: 21, color: "#e78ac3" },
        { id: 5, value: 37, color: "#a6d854" },
        { id: 6, value: 18, color: "#ffd92f" },
        { id: 7, value: 18, color: "#e5c494" },
      ]
    }
  },
  // data() {
  //   return {
  //     bubbles: [
  //       { id: 1, value: 18, color: "#66c2a5" },
  //       { id: 2, value: 19, color: "#fc8d62" },
  //       { id: 3, value: 24, color: "#8da0cb" },
  //       { id: 4, value: 21, color: "#e78ac3" },
  //       { id: 5, value: 37, color: "#a6d854" },
  //       { id: 6, value: 18, color: "#ffd92f" },
  //       { id: 7, value: 18, color: "#e5c494" },
  //     ],
  //   };
  // },
  computed: {
    containerStyle() {
      return {
        width: this.width,
        height: this.height,
      };
    },
  },
  mounted() {
    this.drawBubbleChart();
  },
  methods: {
    drawBubbleChart() {
      const data = this.chartData;
      const width = 220;
      const height = 220;

      // Create an SVG container
      const svg = d3
        .select(this.$refs.bubbleChart)
        .append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", `0 0 ${width} ${height}`)
        .attr("preserveAspectRatio", "xMidYMid meet");

      // Create a simulation to arrange bubbles
      const simulation = d3
        .forceSimulation(data)
        .force(
          "charge",
          d3.forceManyBody().strength(5) // Repel the bubbles slightly
        )
        .force(
          "center",
          d3.forceCenter(width / 2, height / 2) // Center the chart
        )
        .force(
          "collision",
          d3.forceCollide().radius((d) => d.value + 5) // Prevent overlap
        )
        .on("tick", () => {
          circles
            .attr("cx", (d) => d.x)
            .attr("cy", (d) => d.y);
          labels
            .attr("x", (d) => d.x)
            .attr("y", (d) => d.y + 5);
        });

      // Add circles for bubbles
      const circles = svg
        .selectAll("circle")
        .data(data)
        .enter()
        .append("circle")
        .attr("r", (d) => d.value)
        .attr("fill", (d) => d.color)
        .attr("stroke", "#000")
        .attr("stroke-width", 1.5);

      // Add labels inside circles
      const labels = svg
        .selectAll("text")
        .data(data)
        .enter()
        .append("text")
        .text((d) => d.value)
        .attr("text-anchor", "middle")
        .attr("alignment-baseline", "middle")
        .attr("font-size", "14px")
        .attr("fill", "#fff");
    },
  },
};
</script>

<style>
.bubble-chart {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 220px;
  height: 220px;
}
</style>