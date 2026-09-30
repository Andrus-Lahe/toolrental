<template>
  <header class="border-bottom bg-white">
    <nav class="container d-flex align-items-center gap-4 py-3" aria-label="Peamine navigatsioon">
      <RouterLink to="/" class="fw-bold text-decoration-none">Laenukas</RouterLink>
      <RouterLink to="/">Avaleht</RouterLink>
      <RouterLink to="/ai-search">AI otsing</RouterLink>
      <button
        v-if="session.status === 'guest'"
        type="button"
        class="btn btn-outline-primary ms-auto"
        @click="openLoginModal"
      >
        Logi sisse / Registreeru
      </button>
      <span v-else-if="session.status === 'authenticated'" class="ms-auto">
        {{ session.user.firstName }} {{ session.user.lastName }}
      </span>
    </nav>
  </header>
</template>

<script>
import { RouterLink } from 'vue-router'
import { session } from '@/auth/session.js'

export default {
  name: 'AppHeader',
  components: { RouterLink },
  inject: ['openLoginModal'],
  data() {
    return { session }
  },
}
</script>
