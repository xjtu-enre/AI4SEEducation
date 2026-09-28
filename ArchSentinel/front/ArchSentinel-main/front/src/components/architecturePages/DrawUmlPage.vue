<template>
  <div class="UML">
    <architecture-violation-card
      :project-name="'New Project'"
      :project-scan-uuid="scanUuid"
      :uml-pic-dir="plantUmlUrl"
    />
    <div class="content-container">
      <!-- 左侧布局 -->
      <div class="left-panel">
        <div class="uml-window">
          <textarea id="uml" v-model="plantUmlText"></textarea>
        </div>
        <div class="input-container">
          <div class="input-group">
            <span>URL:</span>
            <input id="url" v-model="plantUmlUrl" placeholder="请输入URL地址" />
          </div>
          <div class="input-group">
            <span>NL:</span>
            <input id="nl" v-model="nlText" placeholder="请输入自然语言描述" />
          </div>
        </div>
      </div>

      <!-- 右侧布局 -->
      <div class="right-panel">
        <div class="image-container">
          <img id="image" :src="plantUmlUrl" alt="PlantUML Diagram" />
        </div>
        <div class="button-container">
          <button class="submit-btn" @click="handleSubmit">提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { watch, ref } from 'vue'
// import Plantuml from '@/components/Plantuml.vue';
import ArchitectureViolationCard from '@/components/architectureComponents/ArchitectureViolationCard.vue'
import PlantUmlEncoder from 'plantuml-encoder'
import { useScanStatus } from '@/components/architectureComponents/script/architecture'

const urlParams = new URLSearchParams(window.location.search)
const scanUuid = urlParams.get('scanuuid')
const nlText = ref('')
const showNlResult = ref(false)

// const violationScanUuid = ref(null);
// const handleViolationScanUuidUpdate = (uuid) => {
//   violationScanUuid.value = uuid;
//   console.log('Received violationScanUuid from child:', uuid);
// }

// Example PlantUML diagram
const plantUmlText = ref(`
  @startuml
  class Public
  class Private
  Public <|-- Private : should not be extended by
  @enduml
  `)

// Encode the diagram and create a URL to render it
const plantUmlUrl = ref(
  `http://www.plantuml.com/plantuml/png/${PlantUmlEncoder.encode(
    plantUmlText.value
  )}`
)

watch(plantUmlText, () => {
  plantUmlUrl.value = `http://www.plantuml.com/plantuml/png/${PlantUmlEncoder.encode(
    plantUmlText.value
  )}`
})

const error = ref(null)
const scanStatus = ref('SUCCESS')

PlantUmlEncoder.encode(`
  @startuml
  Alice -> Bob: Hello
  Bob -> Alice: Hi
  @enduml
  `)

const handleSubmit = () => {
  console.log('自然语言描述:', nlText.value)
}
</script>

<style scoped>
.UML {
  height: 100vh;
  width: 100%;
  background-color: #f0f0f0;
  display: flex;
  flex-direction: column;
}

.content-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 80px);
  flex: 1;
  padding-left: 80px;
}

.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 45%;
  padding-left: 80px;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 45%;
  height: 100%;
  margin-top: -120px;
}

.uml-window {
  flex: 2;
}

#uml {
  width: 100%;
  height: 100%;
  border: 2px solid black;
  padding: 10px;
  resize: none;
}

.input-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 30px;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-group span {
  color: #000000;
  font-weight: 500;
  min-width: 50px;
}

#url,
#nl {
  flex: 1;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.image-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  max-height: 90vh;
  margin-top: 0;
}

#image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transform: scale(1.4);
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  position: relative;
  top: -200px;
}

.submit-btn {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.submit-btn:hover {
  background-color: #0056b3;
}

.nl-result {
  margin-top: 20px;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
