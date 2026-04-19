async function apiGet(url) {
    const res = await fetch(url)
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`${res.status} ${text}`)
    }
    return await res.json()
  }
  
  export function getPending() {
    return apiGet('/api/review/pending')
  }
  
  export function getReviewDetail(id) {
    return apiGet(`/api/review/resources/${id}`)
  }