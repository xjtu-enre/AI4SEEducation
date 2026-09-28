<template>
    <div class="section-div-div">

        <metric-evolution-analysis />

        <CouplingSurfacesAnalysis />

        <OwnershipDataAnalysis />

        <refactoring-miner-analysis />

        <intrusive-modify-analysis />

        <!-- <div class="div2-advice">
            <div class="div2-advice-rate">
                <span>评分：</span>
                <el-rate v-model="ArchVitalsRating" :colors="rateColors" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            </div>
            <div class="div2-advice-text">
                <span>建议：{{ ArchVitalsAdvice }}</span>
            </div>
        </div> -->
    </div>
</template>

<script>
import MetricEvolutionAnalysis from './ArchVitals/MetricEvolutionAnalysis.vue'
import CouplingSurfacesAnalysis from './ArchVitals/CouplingSurfacesAnalysis.vue'
import IntrusiveModifyAnalysis from './ArchVitals/IntrusiveModifyAnalysis.vue'
import OwnershipDataAnalysis from './ArchVitals/OwnershipDataAnalysis.vue'
import RefactoringMinerAnalysis from './ArchVitals/RefactoringMinerAnalysis.vue'

export default {
    components: {MetricEvolutionAnalysis, CouplingSurfacesAnalysis, OwnershipDataAnalysis, RefactoringMinerAnalysis, IntrusiveModifyAnalysis},
}
</script>

<style scoped>
.section-div-div{
    display: flex;
    flex-direction: column;
    width: 100%;
    color: black;
}

.div2-top {
    width: 100%;
    display: flex;
    flex-direction: column;
    /* height: 200px; */
    /* margin-bottom: 5%; */
}

.div2-top span{
    display: flex;
    text-align: left;
    margin-bottom: 1%;
}

.div2-top .title{
    font-size: 1.1rem;
}

.div2-bottom {
    width: 100%;
    display: flex;
    flex-direction: column;
    /* height: 450px; */
    margin-bottom: 5%;
}

.chart-title{
    font-size: 1.1rem;
}
</style>