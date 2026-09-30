<template>
  <nav class="navbar navbar-expand-lg bg-body-tertiary border-bottom px-3 mb-3">
    <RouterLink class="navbar-brand" to="/">
      <img src="@/assets/logo.png" alt="Laenukas" height="40" />
    </RouterLink>
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
      <div class="navbar-nav mx-auto">
        <RouterLink class="nav-link" active-class="active" to="/">Avaleht</RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/tools">Otsi tööriistu</RouterLink>
        <RouterLink v-if="isLoggedIn" class="nav-link" active-class="active" to="/my-tools">
          Minu tööriistad
        </RouterLink>
        <RouterLink v-if="isLoggedIn" class="nav-link" active-class="active" to="/profile">
          Profiil
        </RouterLink>
        <RouterLink v-if="isAdmin" class="nav-link" active-class="active" to="/admin">
          Admin
        </RouterLink>
      </div>

      <div v-if="!userState.isLoading" class="d-flex align-items-center gap-2">
        <template v-if="userState.errorMessage">
          <span class="text-danger small">{{ userState.errorMessage }}</span>
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="loadCurrentUser">
            Proovi uuesti
          </button>
        </template>

        <form v-else-if="isLoggedIn" method="post" action="/logout">
          <button type="submit" class="btn btn-outline-dark">Logi välja</button>
        </form>

        <a v-else class="btn btn-outline-dark" href="/oauth2/authorization/google">
          Logi sisse / Registreeru
        </a>
      </div>
    </div>
  </nav>
</template>

<script>
import userState from '@/auth/UserState.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'AppHeader',
  data() {
    return {
      userState: userState,
    }
  },
  computed: {
    isLoggedIn() {
      return this.userState.isLoggedIn()
    },

    isAdmin() {
      return this.userState.isAdmin()
    },
  },
  methods: {
    loadCurrentUser() {
      this.userState
        .loadCurrentUser()
        .then(() => this.$router.isReady())
        .then(() => this.handleProfileRedirect())
    },

    // Profiilita kasutaja suunatakse profiili täitma, kui ta juba seal ei ole (suunamistsüklit ei teki).
    handleProfileRedirect() {
      const currentUser = this.userState.currentUser
      if (currentUser && !currentUser.hasProfile && this.$route.name !== 'profileRoute') {
        NavigationService.navigateToProfileView()
      }
    },
  },
  beforeMount() {
    this.loadCurrentUser()
  },
}
</script>
