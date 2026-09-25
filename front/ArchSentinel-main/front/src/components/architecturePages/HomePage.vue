<script>
import DialogBox from '@/components/architectureComponents/DialogBox.vue'
import ArchitectureInfomationCard from '@/components/architectureComponents/ArchitectureInfomationCard.vue'
import { useProjects, useTrigger } from '@/components/architectureComponents/script/architecture'

export default {
  components: {
    DialogBox,
    ArchitectureInfomationCard,
  },
  data() {
    return {
      projectMap: useProjects(), // Vue 2 应该是响应式对象
      showDialog: false,
      codeDir: null,
      gapConfNames: null,
      gapConfs: [
        { name: 'MH', isChecked: false },
        { name: 'CH', isChecked: false },
        { name: 'CD', isChecked: false },
        { name: 'AWD', isChecked: false },
      ],
      error: null,
      scanUuid: null,
    }
  },
  computed: {
    hasProjects() {
      return this.projectMap.size > 0
    },
  },
  methods: {
    startCheck() {
      let projectGapConfs = []
      console.log(this.gapConfs)

      for (let i = 0; i < this.gapConfs.length; i++) {
        if (this.gapConfs[i].isChecked) {
          projectGapConfs.push(this.gapConfs[i].name)
        }
      }

      console.log('confNames: ', projectGapConfs)
      const result = useTrigger(this.codeDir, projectGapConfs)
      this.error = result.error
      this.scanUuid = result.scanUuid

      console.log('get scanUuid: ', this.scanUuid)

      this.codeDir = null
      this.gapConfNames = null
      this.showDialog = false
    },
  },
}
</script>

<template>
  <div id="home-page">
    <div>
      <dialog-box v-model:visible="showDialog">
        <template #header>
          <h2 class="dialog-header">New Project</h2>
        </template>
        <template #content>
          <form @submit.prevent="startCheck()">
            <div class="dialog-item">
              <label for="code-path">Code Path:</label>
            </div>
            <input
              type="text"
              id="code-path"
              name="code-path"
              v-model="codeDir"
            /><br />
            <div class="dialog-item">
              <label for="gap">GAP:</label>
            </div>

            <ul style="padding: 0">
              <li v-for="(conf, index) in gapConfs" :key="index" style="list-style: none">
                <label>{{ conf.name }}</label>
                <input type="checkbox" v-model="conf.isChecked" />
              </li>
            </ul>

            <br />
            <button class="dialog-button" @click="showDialog = false">
              Close
            </button>
            <input
              type="submit"
              id="submit-button"
              value="Start Architecture Scan"
            />
          </form>
        </template>

        <template #footer></template>
      </dialog-box>
    </div>

    <div class="buttons">
      <button class="btn" @click="showDialog = true">NEW</button>
      <button class="btn" @click="console.log(projectMap)">
        DEBUG: Print Projects in Console
      </button>
      <button class="btn" @click="console.log(projectMap.size)">
        DEBUG: Print the Number of Projects in Console
      </button>
    </div>

    <!-- 确保 v-if 和 v-else 是相邻的 -->
    <template v-if="hasProjects">
      <architecture-infomation-card
        v-for="([projectKey, projectValue], index) in projectMap"
        :key="projectKey"
        :has-project="true"
        :scan-status="useScanStatus(projectKey)"
        :project-name="projectValue.projectName"
        :project-scan-uuid="projectKey"
        :index="index + 1"
      />
    </template>

    <architecture-infomation-card
      v-else
      :has-project="false"
      :project-name="null"
      :project-scan-uuid="null"
      :index="-1"
      :scan-status="null"
    />
  </div>
</template>

<style scoped>
#home-page {
  font-family: Arial, sans-serif;
  display: block;
  margin: 0 auto;
  padding: 0 20px;
  width: 1150px;
}

.dialog-header {
  font-size: larger;
}

.dialog-item {
  min-width: 100px;
  display: inline-block;
  margin: 10px 0px;
}

form input,
form select {
  background-color: white;
  border: black solid 2px;
  border-radius: 5px;
}

.dialog-button {
  background-color: white;
  border: black solid 2px;
  border-radius: 5px;
  margin: 0px 40px;
  padding: 2px 2px;
}

.header h1 {
  margin-bottom: 10px;
}

.buttons {
  display: block;
  justify-content: left;
}

.btn {
  display: inline;
  margin-right: 10px;
  padding: 3px 10px;
  background-color: white;
  color: #2196f3;
  border: #2196f3 solid 2px;
  border-radius: 5px;
  cursor: pointer;
}
</style>
