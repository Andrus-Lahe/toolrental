import { reactive } from 'vue'
import AuthService from '@/api-services/AuthService.js'

const USER_LOADING_FAILED = 'Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.'

// Ühine kasutajaolek: kõik komponendid loevad sama objekti, et GET /api/me päringut ei korrataks igas vaates.
const userState = reactive({
  isLoading: true,
  errorMessage: '',
  currentUser: null,
  loadingPromise: null,

  // Küsib sisselogitud kasutaja andmed. Kui päring juba käib, tagastatakse sama promise (topeltpäringut ei tehta).
  loadCurrentUser() {
    if (this.loadingPromise) {
      return this.loadingPromise
    }
    this.isLoading = true
    this.errorMessage = ''
    this.loadingPromise = AuthService.sendGetCurrentUserRequest()
      .then((response) => (this.currentUser = response.data))
      .catch((error) => this.handleLoadCurrentUserError(error))
      .finally(() => {
        this.isLoading = false
        this.loadingPromise = null
      })
    return this.loadingPromise
  },

  // 401 tähendab külastajat (currentUser = null), mitte viga.
  // 500 või võrguviga tähendab teadmata sessiooni: kuvatakse veateade ja kasutaja õigusi ei eeldata.
  handleLoadCurrentUserError(error) {
    this.currentUser = null
    if (error.response?.status !== 401) {
      this.errorMessage = error.response?.data?.message ?? USER_LOADING_FAILED
    }
  },

  isLoggedIn() {
    return this.currentUser !== null
  },

  isAdmin() {
    return this.currentUser?.roleName === 'admin'
  },
})

export default userState
