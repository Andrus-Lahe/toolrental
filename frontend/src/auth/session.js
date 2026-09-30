import { reactive } from 'vue'
import AuthService from '@/api-services/AuthService.js'

export const session = reactive({ status: 'loading', user: null, error: '' })

export function loadSession() {
  session.status = 'loading'
  session.error = ''
  return AuthService.sendGetCurrentUserRequest()
    .then((response) => {
      session.user = response.data
      session.status = 'authenticated'
    })
    .catch((error) => {
      if (error.response?.status === 401) {
        session.user = null
        session.status = 'guest'
      } else {
        session.status = 'error'
        session.error = 'Sisselogimise oleku laadimine ebaõnnestus.'
      }
    })
}
