<template>
  <main class="container py-4 py-lg-5">
    <div v-if="successMessage" class="alert alert-success" role="status">
      {{ successMessage }}
    </div>

    <div v-if="toolDeletedMessage" class="alert alert-success" role="status">
      {{ toolDeletedMessage }}
    </div>

    <h1 class="h2 mb-4">Minu tööriistad</h1>

    <section v-if="accessStatus === 'checking'" class="py-4 text-center" role="status">
      Kontrollin kasutaja ligipääsu…
    </section>

    <section v-else-if="accessStatus === 'guest'" class="alert alert-info" role="status">
      <AlertDanger :error-message="errorMessage" />
      <p class="mb-2">Minu tööriistade vaatamiseks logi sisse.</p>
      <button type="button" class="btn btn-primary" @click="openLoginModal">Logi sisse</button>
    </section>

    <section v-else-if="accessStatus === 'session-error'" class="auth-message" role="alert">
      <AlertDanger :error-message="errorMessage" />
      <button type="button" class="btn btn-outline-primary" @click="checkAccess">
        Proovi uuesti
      </button>
    </section>

    <section v-else-if="accessStatus === 'forbidden'" class="alert alert-danger" role="alert">
      Sul puudub ligipääs sellele vaatele.
    </section>

    <template v-else>
      <AlertDanger :error-message="errorMessage" />

      <div v-if="isLoading && !myToolsResponse" class="py-4 text-center" role="status">
        Minu tööriistade laadimine…
      </div>

      <div v-if="loadFailed && !isBlocked" class="mb-4">
        <button
          v-if="!profileRequired"
          type="button"
          class="btn btn-outline-primary"
          :disabled="isLoading"
          @click="handleRetry"
        >
          Proovi uuesti
        </button>
        <button
          v-else
          type="button"
          class="btn btn-outline-primary"
          @click="handleEditProfile"
        >
          Täida profiil
        </button>
      </div>

      <div v-if="myToolsResponse" class="row g-4">
        <aside class="col-12 col-lg-3">
          <UserProfileCard
            :profile="myToolsResponse"
            :is-action-disabled="isBlocked"
            @event-edit-profile="handleEditProfile"
          />
          <button
            v-if="canAddTool"
            type="button"
            class="btn btn-primary btn-sm mt-3"
            :disabled="isBlocked"
            @click="handleAddTool"
          >
            Lisa uus tööriist
          </button>
        </aside>

        <div class="col-12 col-lg-9">
          <MyToolsSection
            title="Minu laenutused"
            heading-id="my-rentals-heading"
            :items="myToolsResponse.myRentals"
            item-type="booking"
            :is-loaded="true"
            :is-details-disabled="isBlocked"
            @event-view-details="(booking) => handleViewBookingDetails(booking)"
          />

          <section class="mb-4" aria-labelledby="pending-requests-heading">
            <h2 id="pending-requests-heading" class="h4 mb-3">Admin. kinnituse ootel</h2>
            <MyToolsSection
              title="Palun kinnita"
              heading-id="incoming-requests-heading"
              :items="myToolsResponse.incomingRequests"
              item-type="booking"
              :is-loaded="true"
              :is-details-disabled="isBlocked"
              @event-view-details="(booking) => handleApproveBooking(booking)"
            />
            <MyToolsSection
              title="Ootab omaniku kinnitust"
              heading-id="outgoing-requests-heading"
              :items="myToolsResponse.outgoingRequests"
              item-type="booking"
              :is-loaded="true"
              :is-details-disabled="isBlocked"
              @event-view-details="(booking) => handleViewBookingDetails(booking)"
            />
          </section>

          <section class="mb-4" aria-labelledby="my-tools-heading">
            <h2 id="my-tools-heading" class="h4 mb-3">Minu tööriistad</h2>
            <MyToolsSection
              title="Vabad"
              heading-id="available-tools-heading"
              :items="myToolsResponse.availableTools"
              item-type="tool"
              :is-loaded="true"
              :is-details-disabled="isBlocked"
              :is-deletable="true"
              :is-delete-disabled="isBlocked || isToolDeleting"
              @event-view-details="(tool) => handleViewToolDetails(tool)"
              @event-delete-tool="(tool) => handleDeleteToolRequested(tool)"
            />
            <MyToolsSection
              title="Välja laenatud"
              heading-id="rented-out-tools-heading"
              :items="myToolsResponse.rentedOutTools"
              item-type="booking"
              :is-loaded="true"
              :is-details-disabled="isBlocked"
              @event-view-details="(booking) => handleViewBookingDetails(booking)"
            />
          </section>
        </div>
      </div>
    </template>

    <ConfirmDeleteModal
      :is-open="toolToDelete !== null"
      title="Kustuta tööriist"
      :message="deleteToolMessage"
      :is-busy="isToolDeleting"
      @event-confirm="handleDeleteToolConfirmed"
      @event-cancel="handleDeleteToolCancelled"
    />
  </main>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import MyToolsSection from '@/components/common/MyToolsSection.vue'
import UserProfileCard from '@/components/common/UserProfileCard.vue'
import ConfirmDeleteModal from '@/components/modals/ConfirmDeleteModal.vue'
import MyToolsService from '@/api-services/MyToolsService.js'
import ToolService from '@/api-services/ToolService.js'
import NavigationService from '@/navigation/NavigationService.js'
import { loadSession, session } from '@/auth/session.js'

const MY_TOOLS_LOADING_FAILED = 'Minu tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const TOOL_DELETING_FAILED = 'Tööriista kustutamine ebaõnnestus. Palun proovi hiljem uuesti.'
const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
const LOGIN_REQUIRED_MESSAGE = 'Sinu sisselogimine on aegunud. Logi sisse ja proovi uuesti.'
const BLOCKED_USER_CODE = 'USER_BLOCKED'
const PROFILE_NOT_FOUND_CODE = 'PROFILE_NOT_FOUND'

export default {
  name: 'MyToolsView',
  components: { AlertDanger, MyToolsSection, UserProfileCard, ConfirmDeleteModal },
  inject: ['openLoginModal'],
  data() {
    return {
      accessStatus: 'checking',
      errorMessage: '',
      myToolsResponse: null,
      isLoading: false,
      loadFailed: false,
      isBlocked: false,
      profileRequired: false,
      requestGeneration: 0,
      toolToDelete: null,
      isToolDeleting: false,
      toolDeletedMessage: '',
      session,
    }
  },
  computed: {
    successMessage() {
      return this.$route.query.successMessage ?? ''
    },
    deleteToolMessage() {
      if (!this.toolToDelete) return ''
      return `Kas oled kindel, et soovid tööriista ${this.toolToDelete.toolName} kustutada? Tööriist ja selle pilt kustutatakse jäädavalt.`
    },
    canAddTool() {
      return this.accessStatus === 'ready' && this.session.user?.roleName === 'customer'
    },
  },
  beforeMount() {
    this.checkAccess()
  },
  beforeUnmount() {
    this.requestGeneration += 1
  },
  methods: {
    checkAccess() {
      this.accessStatus = 'checking'
      this.errorMessage = ''
      loadSession()
        .then(() => this.handleSessionLoaded())
        .catch((error) => this.handleSessionError(error))
    },

    handleSessionLoaded() {
      if (this.session.status === 'error') {
        this.accessStatus = 'session-error'
        this.errorMessage = this.session.error || NETWORK_ERROR_MESSAGE
        return
      }
      if (this.session.status !== 'authenticated') {
        this.accessStatus = 'guest'
        return
      }
      if (!['customer', 'admin'].includes(this.session.user?.roleName)) {
        this.accessStatus = 'forbidden'
        return
      }
      this.accessStatus = 'ready'
      this.getMyTools()
    },

    handleSessionError(error) {
      this.accessStatus = 'session-error'
      this.errorMessage = error?.response?.data?.message ?? NETWORK_ERROR_MESSAGE
    },

    handleRetry() {
      if (this.isLoading || this.isBlocked) return
      this.getMyTools()
    },

    handleEditProfile() {
      if (this.isBlocked) return
      NavigationService.navigateToProfile(this.$router)
    },

    handleAddTool() {
      if (!this.canAddTool || this.isBlocked) return
      NavigationService.navigateToAddTool(this.$router)
    },

    handleViewBookingDetails(booking) {
      if (this.isBlocked || !Number.isInteger(booking?.bookingId)) return
      NavigationService.navigateToBookingDetails(this.$router, booking.bookingId)
    },

    handleApproveBooking(booking) {
      if (this.isBlocked || !Number.isInteger(booking?.bookingId)) return
      NavigationService.navigateToBookingApproval(this.$router, booking.bookingId)
    },

    handleDeleteToolRequested(tool) {
      if (this.isBlocked || this.isToolDeleting || !Number.isInteger(tool?.toolId)) return
      this.toolDeletedMessage = ''
      this.toolToDelete = { toolId: tool.toolId, toolName: tool.toolName }
    },

    handleDeleteToolCancelled() {
      if (!this.isToolDeleting) this.toolToDelete = null
    },

    // Saadab DELETE valitud tööriista ID-ga; edu korral laaditakse loend uuesti, vea korral jääb tööriist alles.
    handleDeleteToolConfirmed() {
      if (!this.toolToDelete || this.isToolDeleting) return
      const { toolId, toolName } = this.toolToDelete
      this.errorMessage = ''
      this.isToolDeleting = true
      ToolService.sendDeleteToolRequest(toolId)
        .then(() => this.handleToolDeleted(toolName))
        .catch((error) => this.handleDeleteToolError(error))
        .finally(() => {
          this.isToolDeleting = false
          this.toolToDelete = null
        })
    },

    handleToolDeleted(toolName) {
      this.toolDeletedMessage = `Tööriist ${toolName} kustutati`
      this.getMyTools()
    },

    handleDeleteToolError(error) {
      const status = error?.response?.status
      const apiError = error?.response?.data
      if (status === 401) {
        this.errorMessage = apiError?.message ?? LOGIN_REQUIRED_MESSAGE
        this.openLoginModal()
        return
      }
      this.errorMessage =
        apiError?.message ?? (error?.response ? TOOL_DELETING_FAILED : NETWORK_ERROR_MESSAGE)
    },

    handleViewToolDetails(tool) {
      if (this.isBlocked || !Number.isInteger(tool?.toolId)) return
      NavigationService.navigateToToolDetail(this.$router, tool.toolId)
    },

    getMyTools() {
      const generation = ++this.requestGeneration
      this.isLoading = true
      this.loadFailed = false
      this.profileRequired = false
      this.errorMessage = ''

      MyToolsService.sendGetMyToolsRequest()
        .then((response) => this.handleMyToolsResponse(response.data, generation))
        .catch((error) => this.handleMyToolsError(error, generation))
        .finally(() => {
          if (generation === this.requestGeneration) this.isLoading = false
        })
    },

    handleMyToolsResponse(response, generation) {
      if (generation !== this.requestGeneration) return
      if (!this.isValidResponse(response)) {
        this.myToolsResponse = null
        this.loadFailed = true
        this.errorMessage = MY_TOOLS_LOADING_FAILED
        return
      }
      this.myToolsResponse = response
    },

    isValidResponse(response) {
      if (!response || typeof response !== 'object') return false
      const listDefinitions = [
        ['myRentals', true],
        ['incomingRequests', true],
        ['outgoingRequests', true],
        ['availableTools', false],
        ['rentedOutTools', true],
      ]
      return listDefinitions.every(([listName, isBookingList]) => {
        const list = response[listName]
        return Array.isArray(list) && list.every((item) => (
          item && Number.isInteger(item.toolId) && typeof item.toolName === 'string'
          && (!isBookingList || Number.isInteger(item.bookingId))
        ))
      })
    },

    handleMyToolsError(error, generation) {
      if (generation !== this.requestGeneration) return
      const status = error?.response?.status
      const apiError = error?.response?.data

      if (status === 401) {
        this.myToolsResponse = null
        this.accessStatus = 'ready'
        this.errorMessage = apiError?.message ?? LOGIN_REQUIRED_MESSAGE
        this.loadFailed = true
        this.openLoginModal()
        return
      }
      if (status === 403 && apiError?.errorCode === BLOCKED_USER_CODE) {
        this.myToolsResponse = null
        this.isBlocked = true
        this.accessStatus = 'ready'
        this.errorMessage = apiError.message ?? 'Sinu konto on blokeeritud.'
        this.loadFailed = true
        return
      }
      if (status === 404 && apiError?.errorCode === PROFILE_NOT_FOUND_CODE) {
        this.myToolsResponse = null
        this.profileRequired = true
        this.errorMessage = apiError.message ?? 'Kasutaja profiili ei leitud.'
        this.loadFailed = true
        return
      }

      this.errorMessage = apiError?.message
        ?? (error?.response ? MY_TOOLS_LOADING_FAILED : NETWORK_ERROR_MESSAGE)
      this.loadFailed = true
    },
  },
}
</script>

<style scoped>
.auth-message {
  max-width: 520px;
}
</style>
