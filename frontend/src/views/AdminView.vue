<template>
  <main class="container py-5">
    <section v-if="accessStatus === 'checking'" class="py-5 text-center" aria-live="polite">
      Kontrollin kasutaja ligipääsu…
    </section>

    <section v-else-if="accessStatus === 'guest'" class="mx-auto auth-message" role="alert">
      <h1>Logi sisse</h1>
      <p>Halduse avamiseks logi sisse admini kontoga.</p>
      <button class="btn btn-primary" type="button" @click="openLoginModal">Logi sisse</button>
    </section>

    <section v-else-if="accessStatus === 'forbidden'" class="mx-auto auth-message" role="alert">
      <h1>Ligipääs puudub</h1>
      <p>Haldus on saadaval ainult adminile.</p>
    </section>

    <section v-else-if="accessStatus === 'error'" class="mx-auto auth-message" role="alert">
      <h1>Kasutaja kontrollimine ebaõnnestus</h1>
      <p>{{ accessError }}</p>
      <button class="btn btn-outline-primary" type="button" @click="checkAccess">
        Proovi uuesti
      </button>
    </section>

    <section v-else aria-labelledby="admin-title">
      <h1 id="admin-title" class="mb-4">Haldus</h1>

      <h2 class="h3 mb-3">Kasutajad</h2>
      <AlertDanger :error-message="errorMessage" />
      <div v-if="reloadFailed" class="alert alert-warning" role="alert">
        Muudatus õnnestus, kuid kasutajate loendi värskendamine ebaõnnestus.
        <button type="button" class="btn btn-link p-0 ms-2" @click="getUsers">Proovi uuesti</button>
      </div>

      <p v-if="isLoading" role="status">Kasutajate laadimine...</p>
      <p v-else-if="users.length === 0 && !loadFailed">Kasutajaid ei ole</p>
      <AdminUsersTable
        v-else-if="users.length > 0"
        :users="users"
        :pending-user-id="pendingUserId"
        @event-block-user="handleBlockUser"
        @event-delete-user="handleDeleteRequested"
      />
      <button
        v-if="loadFailed && !isLoading"
        type="button"
        class="btn btn-outline-primary"
        @click="getUsers"
      >
        Proovi uuesti
      </button>
    </section>

    <ConfirmDeleteModal
      :is-open="userToDelete !== null"
      title="Kustuta kasutaja"
      :message="deleteMessage"
      :is-busy="pendingUserId !== 0"
      @event-confirm="handleDeleteConfirmed"
      @event-cancel="handleDeleteCancelled"
    />
  </main>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import AdminUsersTable from '@/components/tables/AdminUsersTable.vue'
import ConfirmDeleteModal from '@/components/modals/ConfirmDeleteModal.vue'
import AdminUserService from '@/api-services/AdminUserService.js'
import { loadSession, session } from '@/auth/session.js'

const USERS_LOADING_FAILED = 'Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
const ACCESS_DENIED_MESSAGE = 'Ligipääs puudub.'

export default {
  name: 'AdminView',
  components: { AlertDanger, AdminUsersTable, ConfirmDeleteModal },
  inject: ['openLoginModal'],
  data() {
    return {
      accessStatus: 'checking',
      accessError: '',
      users: [],
      isLoading: false,
      loadFailed: false,
      reloadFailed: false,
      errorMessage: '',
      pendingUserId: 0,
      userToDelete: null,
      session,
    }
  },
  computed: {
    deleteMessage() {
      if (!this.userToDelete) return ''
      return `Kas oled kindel, et soovid kasutaja ${this.userToDelete.name} kustutada?`
    },
  },
  methods: {
    checkAccess() {
      this.accessStatus = 'checking'
      this.accessError = ''
      loadSession().then(() => this.handleSessionLoaded())
    },

    handleSessionLoaded() {
      if (this.session.status === 'error') {
        this.accessStatus = 'error'
        this.accessError = this.session.error
      } else if (this.session.status !== 'authenticated') {
        this.accessStatus = 'guest'
      } else if (this.session.user?.roleName !== 'admin') {
        this.accessStatus = 'forbidden'
      } else {
        this.accessStatus = 'allowed'
        this.getUsers()
      }
    },

    getUsers() {
      this.isLoading = true
      this.loadFailed = false
      this.reloadFailed = false
      AdminUserService.sendGetAdminUsersRequest()
        .then((response) => this.handleGetUsersResponse(response.data))
        .catch((error) => this.handleGetUsersError(error))
        .finally(() => (this.isLoading = false))
    },

    handleGetUsersResponse(users) {
      this.users = Array.isArray(users) ? users : []
    },

    // Loendi laadimise vea korral jäävad varem laetud andmed nähtavale.
    handleGetUsersError(error) {
      this.loadFailed = true
      this.handleApiError(error, USERS_LOADING_FAILED)
    },

    handleBlockUser(userId) {
      if (this.pendingUserId !== 0) return
      this.errorMessage = ''
      this.pendingUserId = userId
      AdminUserService.sendPatchUserStatusRequest(userId, 'B')
        .then(() => this.reloadUsersAfterChange())
        .catch((error) => this.handleApiError(error))
        .finally(() => (this.pendingUserId = 0))
    },

    handleDeleteRequested(user) {
      if (this.pendingUserId !== 0) return
      const name = [user.firstName, user.lastName]
        .map((part) => (part ?? '').trim())
        .filter((part) => part !== '')
        .join(' ')
      this.userToDelete = { userId: user.userId, name }
    },

    handleDeleteCancelled() {
      this.userToDelete = null
    },

    handleDeleteConfirmed() {
      if (!this.userToDelete || this.pendingUserId !== 0) return
      this.errorMessage = ''
      const userId = this.userToDelete.userId
      this.pendingUserId = userId
      AdminUserService.sendDeleteUserRequest(userId)
        .then(() => this.reloadUsersAfterChange())
        .catch((error) => this.handleApiError(error))
        .finally(() => this.finishDelete())
    },

    finishDelete() {
      this.pendingUserId = 0
      this.userToDelete = null
    },

    // Muutmine õnnestus; ainult loendi värskendamise tõrge ei tohi väita, et muutmine ebaõnnestus.
    reloadUsersAfterChange() {
      this.reloadFailed = false
      return AdminUserService.sendGetAdminUsersRequest()
        .then((response) => this.handleGetUsersResponse(response.data))
        .catch(() => (this.reloadFailed = true))
    },

    handleApiError(error, fallbackMessage = NETWORK_ERROR_MESSAGE) {
      const response = error.response
      if (!response) {
        this.errorMessage = NETWORK_ERROR_MESSAGE
      } else if (response.status === 401) {
        session.user = null
        session.status = 'guest'
        this.accessStatus = 'guest'
      } else if (response.status === 403 && !response.data?.message) {
        this.accessStatus = 'forbidden'
        this.errorMessage = ACCESS_DENIED_MESSAGE
      } else {
        this.errorMessage = response.data?.message ?? fallbackMessage
      }
    },
  },
  beforeMount() {
    this.checkAccess()
  },
}
</script>

<style scoped>
.auth-message {
  max-width: 520px;
}
</style>
