import { ref, watchEffect, unref } from 'vue'

export function useFetch(url, requestOptions, handleResponse) {
  const data = ref(null)
  const error = ref(null)

  const fetchData = async () => {
    data.value = null
    error.value = null

    try {
      const res = await fetch(unref(url), unref(requestOptions))
      const json = await res.json()
      data.value = await handleResponse(json)
    } catch (e) {
      error.value = e
    }
  }

  watchEffect(() => {
    fetchData()
  })

  return { data, error }
}
