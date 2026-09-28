<template>
  <div class="info-table-container">
    <p class="info-table-title">Show: Functions</p>
    <table>
      <thead>
        <tr>
          <th v-for="(head, index) in tableHeads" :key="index">{{ head }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(rowData, index) in tableData" :key="index">
          <td v-for="(data, index) in rowData" :key="index">{{ data }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  props: ['tableTitle', 'tableHeads', 'tableData'],
}
</script>

<style scoped>
.info-table-container {
  border: 2px black solid;
  position: fixed;
  bottom: 5px;
  width: 70%;
  padding: 10px;
  overflow-y: auto;
  /* Allows scrolling within the div if content is too long */
  max-height: 40%;
  /* Limit the height of the div to 40% of the viewport */
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  text-align: left;
  border: 1px solid #ccc;
}
</style>
