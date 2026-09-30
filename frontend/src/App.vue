<template>
  <AppHeader />

  <div v-if="session.status === 'error'" class="container mt-3 alert alert-warning" role="alert">
    {{ session.error }}
    <button type="button" class="btn btn-link" @click="loadSession">Proovi uuesti</button>
  </div>

  <RouterView />
  <GoogleLoginModal :is-open="isLoginModalOpen" @event-modal-closed="isLoginModalOpen = false" />
</template>

<script>
import { RouterView } from 'vue-router'
import AppHeader from '@/navigation/AppHeader.vue'
import GoogleLoginModal from '@/components/modals/GoogleLoginModal.vue'
import { loadSession, session } from '@/auth/session.js'

export default {
  name: 'App',
  components: { AppHeader, RouterView, GoogleLoginModal },
  provide() {
    return { openLoginModal: () => { this.isLoginModalOpen = true } }
  },
  data() {
    return { isLoginModalOpen: false, session }
  },
  methods: {
    loadSession,
  },
  beforeMount() {
    this.loadSession()
  },
}
</script>
