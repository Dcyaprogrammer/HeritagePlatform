import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getToken, logout, getStoredRoles } from '../api/auth.js'

export function usePublicHeader() {
  const router = useRouter()
  const isLoggedIn = ref(!!getToken())
  const isAdmin = ref(false)
  const isContributor = ref(false)

  function syncRolesFromStorage() {
    const r = getStoredRoles()
    isAdmin.value = r.includes('ADMIN')
    isContributor.value = r.includes('CONTRIBUTOR') || r.includes('ADMIN')
  }

  syncRolesFromStorage()

  function refreshAuth() {
    isLoggedIn.value = !!getToken()
    syncRolesFromStorage()
  }

  function goToAdmin() {
    router.push('/admin/resource-review')
  }
  function goToCreateResource() {
    router.push('/resources/create')
  }
  function goToSubmissions() {
    router.push('/resources/submissions')
  }
  function goToProfile() {
    router.push('/profile')
  }
  function goToLogin() {
    router.push('/login')
  }
  function goToRegister() {
    router.push('/register')
  }
  function handleLogout() {
    logout()
    isLoggedIn.value = false
    isAdmin.value = false
    isContributor.value = false
    router.push('/login')
  }

  return {
    isLoggedIn,
    isAdmin,
    isContributor,
    refreshAuth,
    goToAdmin,
    goToCreateResource,
    goToSubmissions,
    goToProfile,
    goToLogin,
    goToRegister,
    handleLogout,
  }
}
