<template>
  <header class="border-bottom bg-white">
    <nav
      class="navbar navbar-expand-lg container py-3"
      aria-label="Peamine navigatsioon"
    >
      <RouterLink to="/" class="navbar-brand fw-bold">Laenukas</RouterLink>
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navMenu"
        aria-controls="navMenu"
        aria-expanded="false"
        aria-label="Ava menüü"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="navMenu" class="collapse navbar-collapse">
        <div class="navbar-nav gap-lg-3 me-auto">
          <RouterLink class="nav-link" active-class="active" to="/">Avaleht</RouterLink>
          <RouterLink class="nav-link" active-class="active" to="/tools">Otsi tööriistu</RouterLink>
          <RouterLink class="nav-link" active-class="active" to="/ai-search">AI otsing</RouterLink>
          <template v-if="isLoggedIn">
            <RouterLink class="nav-link" active-class="active" to="/my-tools">
              Minu tööriistad
            </RouterLink>
            <RouterLink class="nav-link" active-class="active" to="/profile">Profiil</RouterLink>
          </template>
          <RouterLink v-if="isAdmin" class="nav-link" active-class="active" to="/admin">
            Haldus
          </RouterLink>
        </div>

        <div class="d-flex align-items-center gap-2">
          <template v-if="session.status === 'error'">
            <span class="text-danger small">{{ session.error }}</span>
            <button type="button" class="btn btn-outline-secondary btn-sm" @click="loadSession">
              Proovi uuesti
            </button>
          </template>

          <template v-else-if="isLoggedIn">
            <span class="small text-muted">{{ session.user.firstName }} {{ session.user.lastName }}</span>
            <form method="post" action="/logout">
              <button type="submit" class="btn btn-outline-dark">Logi välja</button>
            </form>
          </template>

          <button
            v-else-if="session.status === 'guest'"
            type="button"
            class="btn btn-outline-primary"
            @click="openLoginModal"
          >
            Logi sisse / Registreeru
          </button>
        </div>
      </div>
    </nav>
  </header>
</template>

<script>
import { RouterLink } from 'vue-router'
import { isAdmin, loadSession, session } from '@/auth/session.js'
import { hasLoginReturnPath } from '@/auth/loginReturnPath.js'

export default {
  name: 'AppHeader',
  components: { RouterLink },
  inject: ['openLoginModal'],
  data() {
    return { session }
  },
  computed: {
    isLoggedIn() {
      return this.session.status === 'authenticated'
    },

    isAdmin() {
      return this.isLoggedIn && isAdmin()
    },
  },
  watch: {
    // Profiilita kasutaja suunatakse profiili täitma; profiilil olles suunamistsüklit ei teki.
    'session.status'() {
      this.handleProfileRedirect()
    },
  },
  methods: {
    loadSession,

    handleProfileRedirect() {
      const user = this.session.user
      if (this.isLoggedIn && !user.hasProfile && !hasLoginReturnPath() && this.$route.name !== 'profileRoute') {
        this.$router.push({ name: 'profileRoute' })
      }
    },
  },
  beforeMount() {
    this.handleProfileRedirect()
  },
}
</script>
