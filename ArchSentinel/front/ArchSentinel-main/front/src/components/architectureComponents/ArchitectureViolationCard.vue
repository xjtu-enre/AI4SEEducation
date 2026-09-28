<template>
  <div class="arch-info-card-container">
    <div>
      <div class="arch-info-card" v-if="unref(error) == null">
        <header></header>

        <main>
          <span class="project-prefix"
            ><button
              @click="check(projectScanUuid, umlPicDir)"
              id="start-check-button"
            >
              Start Check
            </button></span
          >

          <component
            :is="unref(scanStatus) == 'SUCCESS' ? 'router-link' : 'empty'"
            :to="routerPath"
          >
            <span class="project-name">{{ projectName }}</span>
          </component>

          <span class="project-scan-status">{{ scanStatus }}</span>

          <!-- <span class="project-scan-status font-success"
                        v-if="unref (scanStatus) === 'SUCCESS'">SUCCESS</span>
                    <span class="project-scan-status font-failed"
                        v-else-if="unref (scanStatus) === 'FAILED'">FAILED</span>
                    <span
                        v-else-if="unref (scanStatus) == 'PENDING' || unref (scanStatus) == null"></span>
                    <span class="project-scan-status font-pending" v-else>RUNNING...</span> -->
        </main>

        <footer></footer>
      </div>
    </div>
  </div>
</template>

<script>
import { unref } from 'vue'
import {
  useViolationScanStatus,
  useViolationCheck,
} from './script/architecture'

export default {
  props: ['projectName', 'projectScanUuid', 'umlPicDir'],

  data() {
    return {
      error: null,
      violationScanUuid: null,
      scanStatus: '',
      routerPath: '/check-result',
    }
  },

  methods: {
    unref,
    check(projectScanUuid, umlPicDir) {
      console.log('check:', projectScanUuid, umlPicDir)

      const rawValue = unref(
        useViolationCheck(unref(this.projectScanUuid), unref(this.umlPicDir))
      )
      const scanUuid = rawValue.data

      this.$watch(
        () => scanUuid,
        (newVal) => {
          if (newVal) {
            console.log('scanUuid: ', newVal.value)
            this.violationScanUuid = newVal.value
            this.routerPath = `/check-result?scanuuid=${unref(newVal)}`
            this.scanStatus = useViolationScanStatus(newVal.value).status
          }
        },
        { deep: true }
      )
    },
  },

  created() {
    console.log('scanstatus:', this.scanStatus)
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
  margin: 0px auto;
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

#start-check-button {
  display: inline;
  /* width: 60px;
    height: 30px; */
  margin-right: 10px;
  padding: 3px 10px;
  background-color: white;
  color: #2196f3;
  border: #2196f3 solid;
  border-width: 2px;
  border-radius: 5px;
  cursor: pointer;
}

.project-prefix {
  width: 20%;
  text-align: left;
}

.project-name {
  width: 60%;
  text-align: center;
}

.project-scan-status {
  width: 20%;
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
