import axios from 'axios'

export const resultUrls = {
  paths: '/api/results/paths',
  upstreamEnre: '/api/results/enre/upstream',
  downstreamEnre: '/api/results/enre/downstream',
  archViolations: '/api/results/arch-violations',
  pmd: '/api/results/file/pmd',
  facade: '/api/results/file/facade',
  ownership: '/api/results/file/ownership',
  refactor: '/api/results/file/refactor',
  metricsEvolution: '/api/results/file/metrics-evolution',
  metricsDescription: '/api/results/file/metrics-description',
  metricsPre: '/api/results/file/metrics-pre',
  metricsNext: '/api/results/file/metrics-next',
  report: '/api/results/file/report',
}

export const gapResultUrl = type => `/api/results/gap/${type}`
export const intrusiveResultUrl = fileName => `/api/results/intrusive/${encodeURIComponent(fileName)}`

export async function getAnalysisPaths() {
  const { data } = await axios.get(resultUrls.paths)
  return data
}

export async function getJsonResult(url) {
  const { data } = await axios.get(url)
  return data
}

