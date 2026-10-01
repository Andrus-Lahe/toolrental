<template>
  <div class="app-shell">
    <AppHeader />

    <div v-if="hasLoginError" class="app-alerts alert alert-danger" role="alert">
      Google kontoga sisselogimine ebaõnnestus. Palun proovi uuesti.
      <button type="button" class="btn btn-link" @click="handleLoginRetry">Proovi uuesti</button>
    </div>

    <div v-if="session.status === 'error'" class="app-alerts alert alert-warning" role="alert">
      {{ session.error }}
      <button type="button" class="btn btn-link" @click="loadSession">Proovi uuesti</button>
    </div>

    <RouterView />
    <GoogleLoginModal :is-open="isLoginModalOpen" @event-modal-closed="handleLoginModalClosed" />
  </div>
</template>

<script>
import { RouterView } from 'vue-router'
import AppHeader from '@/navigation/AppHeader.vue'
import GoogleLoginModal from '@/components/modals/GoogleLoginModal.vue'
import { loadSession, session } from '@/auth/session.js'
import { clearLoginReturnPath, getLoginReturnPath, rememberLoginReturnPath } from '@/auth/loginReturnPath.js'

export default {
  name: 'App',
  components: { AppHeader, RouterView, GoogleLoginModal },
  provide() {
    return {
      openLoginModal: (returnPath) => {
        rememberLoginReturnPath(returnPath)
        this.isLoginModalOpen = true
      },
    }
  },
  data() {
    return { isLoginModalOpen: false, session }
  },
  computed: {
    // Spring Security suunab ebaõnnestunud OAuth sisselogimise aadressile /?loginError
    hasLoginError() {
      return this.$route.query.loginError !== undefined
    },
  },
  watch: {
    'session.status'(status) {
      if (status === 'authenticated') this.restoreLoginReturnPath()
    },
  },
  beforeMount() {
    this.loadSession()
  },
  methods: {
    loadSession() {
      return loadSession().then(() => this.restoreLoginReturnPath())
    },

    restoreLoginReturnPath() {
      if (this.session.status !== 'authenticated') return
      const returnPath = getLoginReturnPath()
      if (!returnPath) return
      if (this.$route.fullPath === returnPath) {
        clearLoginReturnPath()
        return
      }
      this.$router.replace(returnPath).finally(clearLoginReturnPath)
    },

    handleLoginModalClosed() {
      this.isLoginModalOpen = false
      clearLoginReturnPath()
    },

    handleLoginRetry() {
      this.$router.replace({ path: this.$route.path })
      this.isLoginModalOpen = true
    },
  },
}
</script>
