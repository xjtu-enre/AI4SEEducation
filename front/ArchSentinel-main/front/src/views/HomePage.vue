<template>
    <div class="HomePage">
        <div class="header">
            <div class="titleName">
                <span>ArchSentinel</span>
            </div>
            <div class="navButton-div">
                <button
                    class="navButton"
                    :class="{ navButtonActive: buttonIndex === 0 }"
                    @click="navButtonClick(0)"
                >
                    ArchMapper
                </button>
                <button
                    class="navButton"
                    :class="{ navButtonActive: buttonIndex === 3 }"
                    @click="navButtonClick(3)"
                >
                    ArchInspector
                </button>
                <button
                    class="navButton"
                    :class="{ navButtonActive: buttonIndex === 1 }"
                    @click="navButtonClick(1)"
                >
                    ArchCompass
                </button>
                <button
                    class="navButton"
                    :class="{ navButtonActive: buttonIndex === 2 }"
                    @click="navButtonClick(2)"
                >
                    ArchVitals
                </button>
                <button
                    class="navButton"
                    :class="{ navButtonActive: buttonIndex === 4 }"
                    @click="navButtonClick(4)"
                >
                    ArchDiagnose
                </button>
            </div>
          <!-- <button class="exit-button" @click="onClickedButtonExit" /> -->
        </div>
        <div class="mainBox">
            <!-- 项目数据 -->
            <EnerJava
                @updateData="handleDataUpdate"
                v-if="buttonIndex === 0"
                v-on:close="onButtonIndex"
            />
            <!-- 原ArchCompass -->
            <TwoArchitecture v-if="buttonIndex === 1" v-on:close="onButtonIndex" />
            <!-- 上下游代码冲突定位 -->
            <UDCouplingAntiPattern
                v-if="buttonIndex === 2"
                v-on:close="onButtonIndex"
                :parentData="parentData"
            />
            <!-- 原ArchInspector -->
            <Pmd v-if="buttonIndex === 3" v-on:close="onButtonIndex" />

            <!-- 项目报告 -->
            <Report v-if="buttonIndex === 4" v-on:close="onButtonIndex" />
        </div>
    </div>

</template>

<script>
import EnerJava from '@/components/Enre.vue'
import TwoArchitecture from '@/components/TwoArchitecture.vue'
import UDCouplingAntiPattern from '@/components/UDCouplingAntiPattern.vue'
import Pmd from '@/components/Pmd.vue'
import Report from '@/components/ArchReport.vue'

export default {
    name: 'HomePage',
    components: {
        UDCouplingAntiPattern,
        Pmd,
        TwoArchitecture,
        EnerJava,
        Report,
    },
    data() {
        return {
            parentData: {
                upstreamFilePath: '',
                downstreamFilePath: '',
                upstreamFileEnrePath: '',
                downstreamFileEnrePath: '',
            },
            buttonIndex: 0,
            link: '',
        }
    },
    methods: {
        onButtonIndex(url) {
            this.link = url
            console.log(this.link)
            if (this.link.type === 'ener-java') {
                this.navButtonClick(0)
            } else if (this.link.type === 'CD') {
                this.navButtonClick(1)
            } else if (this.link.type === 'CH') {
                this.navButtonClick(2)
            }
        },
        navButtonClick(index) {
            this.buttonIndex = index
        },
        onClickedButtonExit() {
            this.$router.push('/HomePage')
        },
        // 处理子组件传递的数据
        handleDataUpdate(newData) {
            // 更新父组件的数据
            this.parentData = { ...this.parentData, ...newData }
        },
    },

}
</script>

<style scoped>
.HomePage {
    height: 100%;
    width: 100%;
    display: inline-block;
    background-color: #a8a8a8; /* 灰色底色 */
}

.header {
    width: 100%;
    height: 60px;
    background-color: #2c3e50; /* 深色背景 */
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.navButton {
    padding: 8px 15px;
    font-size: 20px;
    border: none;
    border-radius: 5px;
    background-color: #34495e; /* 深灰色按钮 */
    color: white;
    cursor: pointer;
    transition: background-color 0.3s;

}

.navButton:hover {
    background-color: #1abc9c; /* 按钮悬停颜色 */
}

.navButtonActive {
    background-color: #e74c3c; /* 活跃按钮颜色 */
}
</style>
