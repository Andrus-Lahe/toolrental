<template>
  <form class="booking-form" novalidate @submit.prevent="handleSubmit">
    <h2 class="h5 mb-3">Laenutuse periood</h2>

    <div class="row g-3">
      <div class="col-12 col-sm-6">
        <label class="form-label" for="booking-start-date">Alates</label>
        <input
          id="booking-start-date"
          v-model="startDate"
          class="form-control"
          type="date"
          required
          :disabled="disabled"
          :aria-invalid="Boolean(startDateError)"
          @input="clearDateErrors"
        />
        <div v-if="startDateError" class="form-text text-danger" role="alert">
          {{ startDateError }}
        </div>
      </div>
      <div class="col-12 col-sm-6">
        <label class="form-label" for="booking-end-date">Kuni</label>
        <input
          id="booking-end-date"
          v-model="endDate"
          class="form-control"
          type="date"
          required
          :min="startDate || undefined"
          :disabled="disabled"
          :aria-invalid="Boolean(endDateError)"
          @input="clearDateErrors"
        />
        <div v-if="endDateError" class="form-text text-danger" role="alert">
          {{ endDateError }}
        </div>
      </div>
    </div>

    <div class="mt-4">
      <label class="form-label" for="booking-owner-message">Lisainfo omanikule (valikuline)</label>
      <textarea
        id="booking-owner-message"
        v-model="ownerMessage"
        class="form-control"
        rows="4"
        maxlength="500"
        :disabled="disabled"
        :aria-invalid="Boolean(ownerMessageError)"
      ></textarea>
      <div class="form-text">{{ ownerMessage.length }}/500</div>
      <div v-if="ownerMessageError" class="form-text text-danger" role="alert">
        {{ ownerMessageError }}
      </div>
    </div>

    <div class="d-flex flex-wrap justify-content-end gap-2 mt-4">
      <button class="btn btn-outline-secondary" type="button" :disabled="disabled" @click="$emit('event-cancel')">
        Tühista
      </button>
      <button class="btn btn-primary" type="submit" :disabled="!canSubmit">
        {{ submitting ? 'Saadan…' : 'Saada' }}
      </button>
    </div>
  </form>
</template>

<script>
export default {
  name: 'BookingForm',
  props: {
    disabled: { type: Boolean, default: false },
    submitting: { type: Boolean, default: false },
    submitted: { type: Boolean, default: false },
    available: { type: Boolean, default: true },
  },
  emits: ['event-submit', 'event-cancel'],
  data() {
    return {
      startDate: '',
      endDate: '',
      ownerMessage: '',
      startDateError: '',
      endDateError: '',
      ownerMessageError: '',
    }
  },
  computed: {
    canSubmit() {
      return this.available && !this.disabled && !this.submitting && !this.submitted
    },
  },
  methods: {
    clearDateErrors() {
      this.startDateError = ''
      this.endDateError = ''
    },

    handleSubmit() {
      this.startDateError = ''
      this.endDateError = ''
      this.ownerMessageError = ''

      if (!this.startDate) this.startDateError = 'startDate: on kohustuslik'
      else if (!this.isValidDate(this.startDate)) this.startDateError = 'startDate: vigane kuupäev'

      if (!this.endDate) this.endDateError = 'endDate: on kohustuslik'
      else if (!this.isValidDate(this.endDate)) this.endDateError = 'endDate: vigane kuupäev'
      else if (this.isValidDate(this.startDate) && this.endDate < this.startDate) {
        this.endDateError = "endDate: peab olema startDate'iga samal päeval või hiljem"
      }

      if (this.ownerMessage.length > 500) {
        this.ownerMessageError = 'ownerMessage: ei tohi ületada 500 märki'
      }

      if (this.startDateError || this.endDateError || this.ownerMessageError || !this.canSubmit) return

      this.$emit('event-submit', {
        startDate: this.startDate,
        endDate: this.endDate,
        ownerMessage: this.ownerMessage.trim() ? this.ownerMessage : null,
      })
    },

    isValidDate(value) {
      if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false
      const [year, month, day] = value.split('-').map(Number)
      if (month < 1 || month > 12 || day < 1) return false
      const leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0)
      const daysByMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
      return day <= daysByMonth[month - 1]
    },
  },
}
</script>
