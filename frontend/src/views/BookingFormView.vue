<template>
  <main class="container py-5 booking-page">
    <div v-if="pageStatus === 'checking' || pageStatus === 'loading'" class="py-5 text-center" role="status">
      {{ pageStatus === 'checking' ? 'Kontrollin sisselogimist…' : 'Laen tööriista andmeid…' }}
    </div>

    <section v-else-if="pageStatus === 'guest'" class="alert alert-warning" role="alert">
      <p class="mb-2">Laenutaotluse esitamiseks logi sisse.</p>
      <button class="btn btn-primary" type="button" @click="openLoginModal">Logi sisse</button>
    </section>

    <section v-else-if="pageStatus === 'forbidden'" class="alert alert-danger" role="alert">
      Laenutaotluse esitamine on võimalik Customer või Admin kontoga.
    </section>

    <section v-else-if="pageStatus === 'load-error'" class="alert alert-danger" role="alert">
      <p>{{ loadError }}</p>
      <button class="btn btn-outline-primary" type="button" @click="loadPageData">Proovi uuesti</button>
    </section>

    <section v-else-if="pageStatus === 'invalid-route'" class="alert alert-danger" role="alert">
      {{ loadError }}
    </section>

    <section v-else>
      <h1 class="h2 fw-bold mb-4">Laenutuse taotlus</h1>
      <div class="row g-4">
        <div class="col-12 col-lg-7">
          <div class="border rounded p-3 p-md-4">
            <h2 class="h4">{{ tool.toolName }}</h2>

            <div v-if="!isToolAvailable" class="alert alert-warning" role="alert">
              Tööriist pole hetkel saadaval.
            </div>
            <div v-if="errorMessage" class="alert alert-danger" role="alert">
              {{ errorMessage }}
            </div>

            <BookingForm
              :disabled="isSubmitting || wasSubmitted"
              :submitting="isSubmitting"
              :submitted="wasSubmitted"
              :available="isToolAvailable"
              @event-submit="handleSubmit"
              @event-cancel="handleCancel"
            />
          </div>
        </div>

        <div class="col-12 col-lg-5">
          <OwnerContactCard :owner="owner" />
        </div>
      </div>
    </section>

    <BookingSentModal :is-open="isConfirmationOpen" @event-modal-closed="handleConfirmationClosed" />
  </main>
</template>

<script>
import BookingService from '@/api-services/BookingService.js'
import ToolService from '@/api-services/ToolService.js'
import UserService from '@/api-services/UserService.js'
import BookingForm from '@/components/forms/BookingForm.vue'
import OwnerContactCard from '@/components/common/OwnerContactCard.vue'
import BookingSentModal from '@/components/modals/BookingSentModal.vue'
import NavigationService from '@/navigation/NavigationService.js'
import { loadSession, session } from '@/auth/session.js'

const OWN_TOOL_MESSAGE = 'Enda tööriista ei saa laenata'

export default {
  name: 'BookingFormView',
  components: { BookingForm, OwnerContactCard, BookingSentModal },
  inject: ['openLoginModal'],
  data() {
    return {
      session,
      pageStatus: 'checking',
      loadError: '',
      errorMessage: '',
      tool: null,
      owner: null,
      isSubmitting: false,
      wasSubmitted: false,
      isConfirmationOpen: false,
      loadGeneration: 0,
    }
  },
  computed: {
    toolId() {
      const toolId = Number(this.$route.params.toolId)
      return Number.isInteger(toolId) && toolId > 0 ? toolId : null
    },

    isToolAvailable() {
      return this.tool?.status === 'A'
    },
  },
  watch: {
    '$route.params.toolId'() {
      this.loadPageData()
    },
  },
  beforeMount() {
    this.loadPageData()
  },
  methods: {
    loadPageData() {
      const generation = ++this.loadGeneration
      this.pageStatus = 'checking'
      this.loadError = ''
      this.errorMessage = ''
      this.tool = null
      this.owner = null
      this.wasSubmitted = false
      this.isConfirmationOpen = false

      if (!this.toolId) {
        this.pageStatus = 'invalid-route'
        this.loadError = 'Tööriista ID peab olema positiivne täisarv.'
        return
      }

      loadSession().then(() => {
        if (generation !== this.loadGeneration) return
        this.handleSessionLoaded(generation)
      })
    },

    handleSessionLoaded(generation) {
      if (this.session.status === 'error') {
        this.pageStatus = 'load-error'
        this.loadError = this.session.error
        return
      }
      if (this.session.status !== 'authenticated') {
        this.pageStatus = 'guest'
        return
      }
      if (!['customer', 'admin'].includes(this.session.user?.roleName)) {
        this.pageStatus = 'forbidden'
        return
      }

      this.pageStatus = 'loading'
      ToolService.sendGetToolDetailsRequest(this.toolId)
        .then((response) => this.handleToolLoaded(response.data, generation))
        .catch((error) => this.handleToolLoadError(error, generation))
    },

    handleToolLoaded(tool, generation) {
      if (generation !== this.loadGeneration) return
      if (!tool || !Number.isInteger(Number(tool.ownerId)) || Number(tool.ownerId) <= 0) {
        this.pageStatus = 'load-error'
        this.loadError = 'Tööriista omaniku andmeid ei saanud laadida.'
        return
      }

      this.tool = tool
      UserService.sendGetUserDetailsRequest(tool.ownerId)
        .then((response) => this.handleOwnerLoaded(response.data, generation))
        .catch((error) => this.handleOwnerLoadError(error, generation))
    },

    handleOwnerLoaded(owner, generation) {
      if (generation !== this.loadGeneration) return
      if (!owner || Number(owner.userId) !== Number(this.tool.ownerId)) {
        this.pageStatus = 'load-error'
        this.loadError = 'Tööriista omaniku kontaktandmeid ei saanud laadida.'
        return
      }
      this.owner = owner
      this.pageStatus = 'ready'
    },

    handleOwnerLoadError(error, generation) {
      if (generation !== this.loadGeneration) return
      this.pageStatus = 'load-error'
      this.loadError = error?.response?.data?.message ?? 'Omaniku kontaktandmete laadimine ebaõnnestus. Proovi uuesti.'
    },

    handleToolLoadError(error, generation) {
      if (generation !== this.loadGeneration) return
      this.pageStatus = 'load-error'
      this.loadError = error?.response?.data?.message ?? 'Tööriista laadimine ebaõnnestus. Proovi uuesti.'
    },

    handleSubmit(formData) {
      if (this.isSubmitting || this.wasSubmitted || !this.isToolAvailable) return
      this.errorMessage = ''
      this.isSubmitting = true
      const request = {
        toolId: this.toolId,
        startDate: formData.startDate,
        endDate: formData.endDate,
        ownerMessage: formData.ownerMessage,
      }

      BookingService.sendCreateBookingRequest(request)
        .then(() => this.handleBookingCreated())
        .catch((error) => this.handleBookingError(error))
        .finally(() => {
          this.isSubmitting = false
        })
    },

    handleBookingCreated() {
      this.wasSubmitted = true
      this.isConfirmationOpen = true
    },

    handleBookingError(error) {
      const apiError = error?.response?.data
      const status = error?.response?.status
      if (apiError?.errorCode === 'OWN_TOOL_BOOKING_FORBIDDEN') {
        this.errorMessage = OWN_TOOL_MESSAGE
      } else if (apiError?.message) {
        this.errorMessage = apiError.message
      } else if (status === 401) {
        this.errorMessage = 'Sinu sisselogimine on aegunud. Logi sisse ja proovi uuesti.'
        this.openLoginModal()
      } else if (status === 403) {
        this.errorMessage = 'Sul puudub õigus laenutaotlust esitada.'
      } else if (!error?.response) {
        this.errorMessage = 'Serveriga ei saadud ühendust. Taotlust ei saadetud uuesti.'
      } else {
        this.errorMessage = 'Laenutaotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti.'
      }
    },

    handleCancel() {
      if (this.isSubmitting || this.wasSubmitted) return
      NavigationService.navigateBackOrToToolDetail(this.$router, this.toolId)
    },

    handleConfirmationClosed() {
      this.isConfirmationOpen = false
      NavigationService.navigateToMyTools(this.$router)
    },
  },
}
</script>

<style scoped>
.booking-page > section {
  max-width: 1120px;
  margin-inline: auto;
}
</style>
