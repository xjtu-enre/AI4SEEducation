<template>
  <button @click="trigger()"></button>
</template>

<script>
import Plantuml from '@/components/architectureComponents/Plantuml.vue'
import ArchitectureViolationCard from '@/components/architectureComponents/ArchitectureViolationCard.vue'

export default {
  components: {
    Plantuml,
    ArchitectureViolationCard,
  },
  data() {
    return {
      variable: 123,
    }
  },
  methods: {
    trigger() {
      console.log('variable = ', this.variable)
      console.log('variable.value = ', this.variable)
    },
  },
}
</script>

<style scoped></style>
