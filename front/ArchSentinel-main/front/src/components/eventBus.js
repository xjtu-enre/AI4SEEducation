import { reactive } from 'vue';

export const EventBus = reactive({

  pmdList: [],
  upstreamFilePath: '', // 上游项目文件路径
  downstreamFilePath: '', // 下游项目文件路径
  upstreamFileEnrePath: '', // 上游项目Enre分析文件路径
  downstreamFileEnrePath: '', // 下游项目Enre分析文件路径

  UpFileInfo: null,
  DownFileInfo: null,
  antiPatternList: [],
  customAntiPatternList: [],

  // 项目基础信息的实体和关系类型
  upData: null,
  downData: null,

  // 自定义反模式
  ViolationInfoList: [],
  ruleTypeCounts: {},

  // 上下游代码定位
  metriData: [],
  CFList: [],
  OSList: [],
  ownershipTypeCounts: {},
  categoryTypeCounts: {},
  refactList: [],
  intrusiveList: [],
  intrusiveTypeCounts: [],
});
if (import.meta.hot) {
  import.meta.hot.accept();
}

export default EventBus;