<template>
  <form class="booking-decision-form" novalidate @submit.prevent="submitDecision('confirm')">
    <div>
      <label class="form-label" for="booking-decision-message">Lisainfo laenajale (valikuline)</label>
      <textarea
        id="booking-decision-message"
        v-model="ownerMessage"
        class="form-control"
        rows="4"
        maxlength="500"
        :disabled="disabled"
        :aria-invalid="Boolean(validationMessage)"
        :aria-describedby="validationMessage ? 'booking-decision-count booking-decision-error' : 'booking-decision-count'"
      ></textarea>
      <div id="booking-decision-count" class="form-text">{{ ownerMessage.length }}/500</div>
      <div v-if="validationMessage" id="booking-decision-error" class="form-text text-danger" role="alert">
        {{ validationMessage }}
      </div>
    </div>

    <div class="d-flex flex-wrap justify-content-end gap-2 mt-4">
      <button class="btn btn-outline-danger" type="button" :disabled="disabled" @click="submitDecision('reject')">
        {{ disabled ? 'Ootan…' : 'Lükka tagasi' }}
      </button>
      <button class="btn btn-primary" type="submit" :disabled="disabled">
        {{ disabled ? 'Ootan…' : 'Kinnita' }}
      </button>
    </div>
  </form>
</template>

<script>
export default {
  name: 'BookingDecisionForm',
  props: {
    disabled: { type: Boolean, default: false },
  },
  emits: ['event-confirm', 'event-reject'],
  data() {
    return {
      ownerMessage: '',
      validationMessage: '',
    }
  },
  methods: {
    submitDecision(decision) {
      if (this.disabled) return
      this.validationMessage = ''
      if (this.ownerMessage.length > 500) {
        this.validationMessage = 'Sõnum võib olla kuni 500 märki'
        return
      }

      const request = { ownerMessage: this.ownerMessage === '' ? null : this.ownerMessage }
      this.$emit(decision === 'confirm' ? 'event-confirm' : 'event-reject', request)
    },
  },
}
</script>
