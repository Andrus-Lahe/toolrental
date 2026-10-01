<template>
  <main class="container py-5 booking-approval-page">
    <AlertDanger :error-message="errorMessage" />

    <div v-if="pageStatus === 'loading'" class="py-5 text-center" role="status">
      Laen broneeringu andmeid…
    </div>

    <section v-else-if="pageStatus === 'unauthorized'" class="text-center py-4">
      <p>Palun logi sisse.</p>
      <button class="btn btn-primary" type="button" @click="openBookingLogin">Logi sisse</button>
    </section>

    <section v-else-if="pageStatus === 'error'" class="text-center py-4">
      <button class="btn btn-outline-primary" type="button" @click="loadBooking">Proovi uuesti</button>
    </section>

    <section v-else-if="pageStatus === 'ready' && booking" class="booking-approval-content">
      <h1 class="h2 fw-bold mb-4">Laenutuse taotlus</h1>
      <div class="row g-4">
        <div class="col-12 col-lg-7">
          <section class="border rounded p-3 p-md-4" aria-labelledby="booking-tool-name">
            <h2 id="booking-tool-name" class="h4 mb-4">{{ booking.toolName }}</h2>

            <div class="row g-3 mb-4">
              <div class="col-12 col-sm-6">
                <h3 class="h6 mb-1">Laenutuse periood: Alates</h3>
                <p class="mb-0">{{ formatDate(booking.startDate) }}</p>
              </div>
              <div class="col-12 col-sm-6">
                <h3 class="h6 mb-1">Kuni</h3>
                <p class="mb-0">{{ formatDate(booking.endDate) }}</p>
              </div>
            </div>

            <div class="mb-4">
              <h3 class="h6">{{ booking.status === 'P' ? 'Laenaja saatis sulle lisainfo' : 'Lisainfo omanikult' }}</h3>
              <p class="mb-0 text-break">{{ booking.ownerMessage || '—' }}</p>
            </div>

            <BookingDecisionForm
              v-if="canDecide"
              :key="bookingId"
              :disabled="isSubmitting"
              @event-confirm="handleConfirm"
              @event-reject="handleReject"
            />
          </section>
        </div>

        <div class="col-12 col-lg-5">
          <BookingContactCard
            :contact-name="booking.contactName"
            :contact-email="booking.contactEmail"
            :contact-phone="booking.contactPhone"
          />
        </div>
      </div>
    </section>

    <BookingDecisionModal
      :is-open="isDecisionModalOpen"
      :decision="completedDecision"
      @event-modal-closed="handleDecisionModalClosed"
    />
  </main>
</template>

<script>
import BookingService from '@/api-services/BookingService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import BookingContactCard from '@/components/common/BookingContactCard.vue'
import BookingDecisionForm from '@/components/forms/BookingDecisionForm.vue'
import BookingDecisionModal from '@/components/modals/BookingDecisionModal.vue'
import NavigationService from '@/navigation/NavigationService.js'

const REQUEST_FAILED = 'Päring ebaõnnestus. Palun proovi uuesti.'
const MAX_BOOKING_ID = 2147483647

export default {
  name: 'BookingApprovalView',
  components: { AlertDanger, BookingContactCard, BookingDecisionForm, BookingDecisionModal },
  inject: ['openLoginModal'],
  data() {
    return {
      pageStatus: 'loading',
      booking: null,
      errorMessage: '',
      isSubmitting: false,
      isDecisionModalOpen: false,
      completedDecision: '',
      decisionBlocked: false,
      loadGeneration: 0,
    }
  },
  computed: {
    bookingId() {
      const routeId = String(this.$route.params.bookingId ?? '')
      if (!/^\d+$/.test(routeId)) return null
      const bookingId = Number(routeId)
      return Number.isSafeInteger(bookingId) && bookingId > 0 && bookingId <= MAX_BOOKING_ID ? bookingId : null
    },
    canDecide() {
      return !this.decisionBlocked && this.booking?.isOwner === true && this.booking?.status === 'P'
    },
  },
  watch: {
    '$route.params.bookingId'() {
      this.loadBooking()
    },
  },
  beforeMount() {
    this.loadBooking()
  },
  beforeUnmount() {
    this.loadGeneration += 1
  },
  methods: {
    openBookingLogin() {
      this.openLoginModal(this.$route.fullPath)
    },

    formatDate(date) {
      if (typeof date !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(date)) return '—'
      const [year, month, day] = date.split('-')
      return `${day}.${month}.${year}`
    },

    handleConfirm(request) {
      return this.submitDecision('confirmed', request)
    },

    handleReject(request) {
      return this.submitDecision('rejected', request)
    },

    handleDecisionModalClosed() {
      this.isDecisionModalOpen = false
      NavigationService.navigateToMyTools(this.$router)
    },

    submitDecision(decision, request) {
      if (this.isSubmitting || !this.canDecide || !this.booking) return
      const ownerMessage = request?.ownerMessage ?? null
      if (typeof ownerMessage === 'string' && ownerMessage.length > 500) {
        this.errorMessage = 'Sõnum võib olla kuni 500 märki'
        return
      }

      const bookingId = this.bookingId
      const generation = this.loadGeneration
      const body = { ownerMessage }
      this.errorMessage = ''
      this.isSubmitting = true

      const requestPromise = decision === 'confirmed'
        ? BookingService.sendConfirmBookingRequest(bookingId, body)
        : BookingService.sendRejectBookingRequest(bookingId, body)

      return requestPromise
        .then((response) => this.handleDecisionResponse(response, decision, body, generation))
        .catch((error) => this.handleDecisionError(error, generation))
        .finally(() => {
          if (generation === this.loadGeneration) this.isSubmitting = false
        })
    },

    handleDecisionResponse(response, decision, request, generation) {
      if (generation !== this.loadGeneration) return
      if (response?.status !== 200 || !this.booking) throw new Error('Decision request failed')
      this.booking.status = decision === 'confirmed' ? 'C' : 'R'
      this.booking.ownerMessage = request.ownerMessage
      this.completedDecision = decision
      this.isDecisionModalOpen = true
    },

    handleDecisionError(error, generation) {
      if (generation !== this.loadGeneration) return
      const status = error?.response?.status
      const apiError = error?.response?.data
      this.errorMessage = apiError?.message || REQUEST_FAILED

      if (status === 401) {
        this.errorMessage = 'Palun logi sisse.'
        this.openLoginModal(this.$route.fullPath)
      } else if (status === 404 || apiError?.errorCode === 'BOOKING_NOT_OWNER') {
        this.booking = null
        this.pageStatus = 'error'
      } else if (apiError?.errorCode === 'BOOKING_NOT_PENDING') {
        this.decisionBlocked = true
        return this.loadBooking({ keepError: true, preserveDecisionBlock: true })
      } else if (status >= 500 || !error?.response) {
        return this.loadBooking({ keepError: true, preserveDecisionBlock: true })
      }
    },

    loadBooking({ keepError = false, preserveDecisionBlock = false } = {}) {
      const generation = ++this.loadGeneration
      const bookingId = this.bookingId
      this.pageStatus = 'loading'
      this.booking = null
      if (!keepError) this.errorMessage = ''
      this.isSubmitting = false
      this.isDecisionModalOpen = false
      this.completedDecision = ''
      if (!preserveDecisionBlock) this.decisionBlocked = false

      if (!bookingId) {
        this.pageStatus = 'invalid-route'
        this.errorMessage = 'Vigane broneeringu ID.'
        return
      }

      return BookingService.sendGetBookingRequest(bookingId)
        .then((response) => this.handleBookingResponse(response, generation))
        .catch((error) => this.handleBookingError(error, generation))
    },

    handleBookingResponse(response, generation) {
      if (generation !== this.loadGeneration) return
      const booking = response?.data
      if (response?.status !== 200 || !this.isValidBooking(booking)) {
        this.pageStatus = 'error'
        this.errorMessage = REQUEST_FAILED
        return
      }
      this.booking = booking
      this.pageStatus = 'ready'
    },

    isValidBooking(booking) {
      const datePattern = /^\d{4}-\d{2}-\d{2}$/
      return Boolean(booking)
        && Number.isInteger(booking.bookingId)
        && booking.bookingId === this.bookingId
        && booking.bookingId > 0
        && Number.isInteger(booking.toolId)
        && booking.toolId > 0
        && typeof booking.toolName === 'string'
        && datePattern.test(booking.startDate)
        && datePattern.test(booking.endDate)
        && typeof booking.status === 'string'
        && typeof booking.isOwner === 'boolean'
        && typeof booking.contactName === 'string'
        && (booking.ownerMessage === null || typeof booking.ownerMessage === 'string')
        && (booking.contactEmail === null || typeof booking.contactEmail === 'string')
        && (booking.contactPhone === null || typeof booking.contactPhone === 'string')
    },

    handleBookingError(error, generation) {
      if (generation !== this.loadGeneration) return
      const status = error?.response?.status
      this.pageStatus = status === 401 ? 'unauthorized' : 'error'
      if (status === 401) {
        this.errorMessage = 'Palun logi sisse.'
      } else {
        this.errorMessage = error?.response?.data?.message || REQUEST_FAILED
      }
    },
  },
}
</script>
