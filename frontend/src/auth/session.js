import { reactive } from 'vue'
import AuthService from '@/api-services/AuthService.js'

const USER_LOADING_FAILED = 'Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.'

export const session = reactive({ status: 'loading', user: null, error: '' })

// Käimasoleva GET /api/me päringu promise, et App ja Header ei teeks sama päringut kaks korda.
let loadingPromise = null

export function loadSession() {
  if (loadingPromise) {
    return loadingPromise
  }
  session.status = 'loading'
  session.error = ''
  loadingPromise = AuthService.sendGetCurrentUserRequest()
    .then((response) => {
      session.user = response.data
      session.status = 'authenticated'
    })
    .catch((error) => {
      session.user = null
      // 401 tähendab külastajat; muu viga tähendab teadmata sessiooni, mitte külalist.
      if (error.response?.status === 401) {
        session.status = 'guest'
      } else {
        session.status = 'error'
        session.error = error.response?.data?.message ?? USER_LOADING_FAILED
      }
    })
    .finally(() => {
      loadingPromise = null
    })
  return loadingPromise
}

export function isAdmin() {
  return session.user?.roleName === 'admin'
}

// Profiili salvestamise järel uuendatakse ühist olekut, et päis ei suunaks profiili uuesti täitma.
export function markProfileCompleted() {
  if (session.user) {
    session.user.hasProfile = true
  }
}
