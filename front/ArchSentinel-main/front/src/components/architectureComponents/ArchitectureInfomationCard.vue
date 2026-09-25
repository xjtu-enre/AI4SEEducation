<template>
  <div class="arch-info-card-container">
    <div v-if="hasProject">
      <component
        :is="unref(scanStatus) == 'SUCCESS' ? 'router-link' : 'div'"
        :to="routerPath"
      >
        <div class="arch-info-card" v-if="unref(error) == null">
          <header></header>

          <main>
            <span class="project-id">{{ index }}</span>
            <span class="project-name">{{ projectName }}</span>

            <!-- <span class="project-scan-status">{{ scanStatus }}</span> -->

            <span
              class="project-scan-status font-success"
              v-if="unref(scanStatus) === 'SUCCESS'"
              >SUCCESS</span
            >
            <span
              class="project-scan-status font-failed"
              v-else-if="unref(scanStatus) === 'FAILED'"
              >FAILED</span
            >
            <span class="project-scan-status font-pending" v-else
              >RUNNING...</span
            >
          </main>

          <footer></footer>
        </div>
      </component>
    </div>

    <div v-if="!hasProject">
      <div class="arch-info-card">
        <header></header>

        <main>
          <span class="project-id"></span>
          <span class="project-name"> No Projects in Workspace </span>
          <span class="project-status"></span>
        </main>

        <footer></footer>
      </div>
    </div>
  </div>
</template>

<script>
import { useScanStatus } from '../architectureComponents/script/architecture'

export default {
  props: ['projectName', 'projectScanUuid', 'index', 'hasProject'],

  data() {
    return {
      error: null,
      scanStatus: null,
    }
  },

  computed: {
    routerPath() {
      return `/entity-relation?scanuuid=${this.projectScanUuid}`
    },
  },

  created() {
    if (this.hasProject) {
      const result = useScanStatus(this.projectScanUuid)
      this.error = result.error
      this.scanStatus = result.status
    }
  },
}
</script>

<style scoped>
.arch-info-card-container {
  /* width: 80%;
margin: auto; */
  display: block;
  flex-wrap: wrap;
}

.arch-info-card {
  background-color: white;
  border-radius: 8px;
  border-width: 1px;
  border: black solid;
  padding: 0px;
  margin: 10px 0px;
  width: 1150px;
}

main {
  padding: 10px;
  margin: auto auto;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 20px;
}

main span {
  display: inline-block;
}

.project-id {
  width: 20%;
  text-align: left;
}

.project-name {
  width: 50%;
  text-align: center;
}

.project-scan-status {
  width: 30%;
  text-align: right;
}

.font-success {
  color: green;
}

.font-failed {
  color: red;
}

.font-pending {
  color: orange;
}

.error {
  text-align: center;
}
</style>
