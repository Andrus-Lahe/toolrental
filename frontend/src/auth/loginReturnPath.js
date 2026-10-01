const STORAGE_KEY = 'toolrental.login-return-path'
const BOOKING_PATH = /^\/bookings\/\d+(?:[?#].*)?$/

export function rememberLoginReturnPath(path) {
  if (typeof path !== 'string' || !BOOKING_PATH.test(path) || typeof window === 'undefined') return
  try {
    window.sessionStorage.setItem(STORAGE_KEY, path)
  } catch {
    // Sisselogimine töötab edasi ka siis, kui brauser ei luba sessionStorage'i kasutada.
  }
}

export function getLoginReturnPath() {
  if (typeof window === 'undefined') return null
  try {
    const path = window.sessionStorage.getItem(STORAGE_KEY)
    return path && BOOKING_PATH.test(path) ? path : null
  } catch {
    return null
  }
}

export function hasLoginReturnPath() {
  if (typeof window === 'undefined') return false
  try {
    return BOOKING_PATH.test(window.sessionStorage.getItem(STORAGE_KEY) || '')
  } catch {
    return false
  }
}

export function clearLoginReturnPath() {
  if (typeof window === 'undefined') return
  try {
    window.sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // Puhastamine ei tohi takistada modalist väljumist.
  }
}
