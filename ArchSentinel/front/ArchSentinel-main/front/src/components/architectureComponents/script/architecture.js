import { ref, watch, watchEffect, onMounted, unref } from 'vue'
import { useFetch } from './fetch.js'
import axios from 'axios'
import { geoProjectionMutator } from 'd3'

var myHeaders = new Headers()
myHeaders.append('User-Agent', 'Apifox/1.0.0 (https://apifox.com)')
myHeaders.append('Content-Type', 'application/json')
myHeaders.append('Accept', '*/*')
myHeaders.append('Host', 'localhost:8080')
myHeaders.append('Connection', 'keep-alive')

const getRequestOptions = function (requestMethod, requestPramas) {
  if (requestMethod == 'POST') {
    return {
      method: requestMethod,
      headers: myHeaders,
      body: JSON.stringify({
        items: [requestPramas],
      }),
      redirect: 'follow',
    }
  } else {
    return {
      method: requestMethod,
      headers: myHeaders,
      redirect: 'follow',
    }
  }
}
const projects = ref(new Map())

// const testMap = new Map();
// testMap.set('test', 111);
// console.log('map:', Array.from(testMap.entries()));
// console.log('map:', new Map(JSON.parse(JSON.stringify(Array.from(testMap.entries())))));

localStorage.setItem(
  'projects',
  JSON.stringify(Array.from(unref(projects).entries()))
)

export function useStoreProjects(newProjects) {
  console.log('storeProject: ', newProjects)
  localStorage.setItem(
    'projects',
    JSON.stringify(Array.from(unref(newProjects)))
  )
}

export function useProjects() {
  // Load projects from localStorage when the composable is used
  const storedProjects = localStorage.getItem('projects')
  console.log('storedProjects=', storedProjects)
  if (storedProjects) {
    projects.value = new Map(JSON.parse(storedProjects))
  }

  // Watch for changes to projects and update localStorage
  watch(
    projects,
    (newProjects) => {
      if (projects.value) {
        localStorage.setItem(
          'projects',
          JSON.stringify(Array.from(unref(newProjects).entries()))
        )
      }
    },
    { deep: true }
  )

  console.log('test: projects value:', Array.from(unref(projects).entries()))

  return projects
}

/**
 *
 * @param {*} githubUrl
 * @param {Array} gapConfNames
 * @returns
 */
export function useTrigger(githubUrl, gapConfNames) {
  const githubUrlValue = unref(githubUrl)
  const gapConfNamesValue = gapConfNames
  console.log(gapConfNames)

  const projectUuid = new Date().getTime()
  let requestUrl = `/architecture/scan/trigger?projectUuid=${projectUuid}&scanTypes=ARCHITECTURE_SCAN`
  console.log('test', gapConfNamesValue)
  for (let i = 0; i < gapConfNamesValue.length; i++) {
    requestUrl = requestUrl.concat(`&gapConfNames=${gapConfNamesValue[i]}`)
  }
  requestUrl = requestUrl.concat(`&priority&githubUrl&umlPicDir`)

  const handleResponse = (json) => {
    try {
      return json.data.items[0].scanUuid
    } catch (err) {
      throw err
    }
  }
  const { data, error } = useFetch(
    requestUrl,
    getRequestOptions('POST', {
      projectUuid: projectUuid,
      priority: 'NORMAL',
      scanTypes: ['ARCHITECTURE_SCAN'],
      githubUrl: githubUrlValue,
      gapConfNames: gapConfNamesValue,
    }),
    handleResponse
  )
  const scanUuid = ref(null)

  watch(data, () => {
    if (data.value) {
      scanUuid.value = data.value
      projects.value.set(scanUuid.value, {
        projectName: 'New Project',
        projectUuid: projectUuid,
      })
      useStoreProjects(projects)
      console.log('after trigger, projects: ', useProjects().value)
      console.log('in useTrigger, scanUuid =', scanUuid.value)
    }

    if (error.value) {
      console.error('in trigger:', error.value)
    }
  })

  return { scanUuid, error }
}

export function useTriggerLocal(localCodePath, gapConfNames) {
  const localCodePathValue = unref(localCodePath)
  const gapConfNamesValue = gapConfNames
  console.log(gapConfNames)

  const projectUuid = new Date().getTime()
  let requestUrl = `/architecture/scan/trigger?projectUuid=${projectUuid}&scanTypes=ARCHITECTURE_SCAN`
  console.log('test', gapConfNamesValue)
  for (let i = 0; i < gapConfNamesValue.length; i++) {
    requestUrl = requestUrl.concat(`&gapConfNames=${gapConfNamesValue[i]}`)
  }
  requestUrl = requestUrl.concat(`&priority&localCodePath&umlPicDir`)

  const handleResponse = (json) => {
    try {
      return json.data.items[0].scanUuid
    } catch (err) {
      throw err
    }
  }
  const { data, error } = useFetch(
    requestUrl,
    getRequestOptions('POST', {
      projectUuid: projectUuid,
      priority: 'NORMAL',
      scanTypes: ['ARCHITECTURE_SCAN'],
      localCodePath: localCodePathValue,
      gapConfNames: gapConfNamesValue,
    }),
    handleResponse
  )
  console.log(localCodePathValue)
  const scanUuid = ref(null)

  watch(data, () => {
    if (data.value) {
      scanUuid.value = data.value
      projects.value.set(scanUuid.value, {
        projectName: 'New Project',
        projectUuid: projectUuid,
      })
      useStoreProjects(projects)
      console.log('in useTrigger, scanUuid =', scanUuid.value)
    }

    if (error.value) {
      console.error('in trigger:', error.value)
    }
  })

  return { scanUuid, error }
}

export function useQueryByScanUuid(scanUuid) {
  if (scanUuid == null) {
    throw Error('scanUuid is null')
  }
  const { data, error } = useFetch(
    `/architecture/stat/${scanUuid}?scanUuid=${scanUuid}`,
    getRequestOptions('GET', null),
    (json) => {
      console.log('useQueryByScanUuid: json =', json)
      return json.data
    }
  )

  return { data, error }
}

export function useQueryLineByScanUuid(scanUuid, pageNum, pageSize) {
  if (scanUuid == null) {
    throw Error('useQueryLineByScanUuid: scanUuid is null')
  }
  const { data, error } = useFetch(
    `/architecture/scc?scanUuid=${scanUuid}&pageNum=${pageNum}&pageSize=${pageSize}`,
    getRequestOptions('GET', null),
    (json) => {
      console.log('useQueryLineByScanUuid: json =', json)
      return json.data
    }
  )

  return { data, error }
}

export function useScanStatus(scanUuid) {
  const status = ref(null)
  const error = ref(null)

  const fetchScanStatus = async () => {
    try {
      console.log('in useScanStatus scanUuid =', unref(scanUuid))
      const response = await axios.get(
        `/architecture/scan/status?scanUuid=${unref(scanUuid)}`
      )
      status.value = response.data.status
      if (status.value != null) {
        console.log('scan stauts:', status.value, 'error', error.value)
        console.log("status.value == 'SUCCESS':", status.value == 'SUCCESS')
        console.log('error.value == null', error.value == null)
        if (
          status.value == 'SUCCESS' ||
          status.value == 'FAILED' ||
          error.value != null
        ) {
          clearInterval(interval)
        }
      }
    } catch (err) {
      error.value = err
      console.error('Failed to fetch scan status:', err)
    }
  }

  const interval = setInterval(fetchScanStatus, 5000) // 每 5 秒轮询一次

  return { status, error }
}

export function useViolationScanStatus(scanUuid) {
  const status = ref(null)
  const error = ref(null)

  const fetchScanStatus = async () => {
    try {
      console.log('in useScanStatus scanUuid =', unref(scanUuid))
      const response = await axios.get(
        `/architecture/checks/status?scanUuid=${unref(scanUuid)}`
      )
      status.value = response.data.status
      if (status.value != null) {
        console.log('scan stauts:', status.value, 'error', error.value)
        console.log("status.value == 'SUCCESS':", status.value == 'SUCCESS')
        console.log('error.value == null', error.value == null)
        if (
          status.value == 'SUCCESS' ||
          status.value == 'FAILED' ||
          error.value != null
        ) {
          clearInterval(interval)
        }
      }
    } catch (err) {
      error.value = err
      console.error('Failed to fetch scan status:', err)
    }
  }

  const interval = setInterval(fetchScanStatus, 5000) // 每 5 秒轮询一次

  return { status, error }
}

export function useQueryEntitiesByCategory(scanUuid, category) {
  const requestUrl = `/architecture/entities/category?scanUuid=${scanUuid}&category=${category}`
  const { data, error } = useFetch(
    requestUrl,
    getRequestOptions('GET', null),
    (json) => {
      console.log('useQueryEntitiesByCategory: json =', json)
      return json.data
    }
  )

  return { data, error }
}

export function useViolationCheck(scanUuid, umlPicDir) {
  console.log()
  console.log('useprojects:', useProjects())
  const project = useProjects().value.get(scanUuid)
  const projectName = project.projectName
  const projectUuid = project.projectUuid

  // TODO: gapConf
  const requestUrl = `/architecture/scan/trigger?projectUuid=${projectUuid}&scanTypes=ARCHITECTURE_SCAN&gapConfNames=MH&priority&codeDir&umlPicDir&archCallbackUrl`
  const handleResponse = (json) => {
    try {
      console.log(json.data.items[0])
      return json.data.items[0].scanUuid
    } catch (err) {
      throw err
    }
  }

  const { data, error } = useFetch(
    requestUrl,
    getRequestOptions('POST', {
      projectUuid: unref(projectUuid),
      priority: 'NORMAL',
      scanTypes: ['ARCHITECTURE_CONFERENCE_CHECK'],
      projectName: unref(projectName),
      umlPicDir: unref(umlPicDir),
    }),
    handleResponse
  )

  watch(data, () => {
    if (data.value) {
      console.log('in violationCheck, data =', data.value)
    }

    if (error.value) {
      console.error('in violationCheck:', error.value)
    }
  })

  return { data, error }
}

export function useViolationCheckResult(scanUuid) {
  const requestUrl = `/architecture/checks/results?scanUuid=${unref(scanUuid)}`
  const requestOptions = getRequestOptions('GET', null)
  const handleResponse = (json) => {
    try {
      console.log('Violation Check Results:', json.data.archunitResults)
      return json.data.archunitResults
    } catch (err) {
      throw err
    }
  }
  const { data, error } = useFetch(requestUrl, requestOptions, handleResponse)

  return { data, error }
}
