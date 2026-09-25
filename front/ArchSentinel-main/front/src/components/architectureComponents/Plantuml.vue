<template>
    <div>
    <textarea id="uml" v-model="plantUmlText"></textarea>
    <div class="url-container">
        <span>URL: </span><input id="url" v-model="plantUmlUrl"></input>
    </div>
    <div id="image-container">
        <img id="image" :src="plantUmlUrl" alt="PlantUML Diagram" />
    </div>
    </div>
</template>

<script>
import plantumlEncoder from 'plantuml-encoder';

export default {
  data() {
    return {
      plantUmlText: `
        @startuml
        Alice -> Bob: Hello
        Bob -> Alice: Hi
        @enduml
      `,
      plantUmlUrl: ''
    };


<style scoped>
#uml {
    margin: 0 auto;
    display: block;
    width: 1150px;
    height: 600px;
    border: 2px black solid;
}

#url {
    /* margin: 0 auto; */
    width: 1150px;

}

.url-container {
    margin: 10px auto;
    display: flex;
    justify-content: center;
}

#image {
    margin: 0 auto;
    display: block;
}
</style>