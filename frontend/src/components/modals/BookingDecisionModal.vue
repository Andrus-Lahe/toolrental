<template>
  <div
    v-if="isOpen"
    class="booking-decision-modal-backdrop"
    role="presentation"
    @click.self="closeModal"
  >
    <section class="booking-decision-modal card shadow" role="dialog" aria-modal="true" aria-labelledby="booking-decision-title">
      <div class="card-body">
        <button class="btn-close float-end" type="button" aria-label="Sulge" @click="closeModal"></button>
        <h2 id="booking-decision-title" class="h4 pe-4">{{ title }}</h2>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'BookingDecisionModal',
  props: {
    isOpen: { type: Boolean, default: false },
    decision: { type: String, default: '' },
  },
  emits: ['event-modal-closed'],
  computed: {
    title() {
      return this.decision === 'rejected' ? 'Taotlus tagasi lükatud' : 'Taotlus kinnitatud'
    },
  },
  watch: {
    isOpen(isOpen) {
      if (isOpen) window.addEventListener('keydown', this.handleKeydown)
      else window.removeEventListener('keydown', this.handleKeydown)
    },
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeydown)
  },
  methods: {
    handleKeydown(event) {
      if (event.key === 'Escape') this.closeModal()
    },
    closeModal() {
      this.$emit('event-modal-closed')
    },
  },
}
</script>

<style scoped>
.booking-decision-modal-backdrop {
  position: fixed;
  z-index: 1055;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}

.booking-decision-modal {
  width: min(100%, 560px);
}
</style>
