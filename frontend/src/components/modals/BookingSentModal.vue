<template>
  <div
    v-if="isOpen"
    class="booking-modal-backdrop"
    role="presentation"
    @click.self="closeModal"
    @keydown.esc="closeModal"
  >
    <section class="booking-modal card shadow" role="dialog" aria-modal="true" aria-labelledby="booking-sent-title">
      <div class="card-body">
        <button class="btn-close float-end" type="button" aria-label="Sulge" @click="closeModal"></button>
        <h2 id="booking-sent-title" class="h4 pe-4">Taotlus saadetud</h2>
        <p class="mb-0">
          Sinu laenutamise taotlus on edukalt saadetud tööriista omanikule ja ootab omaniku kinnitust.
          Teavitame sõnumi teel, kui taotlus on kinnitatud või tagasi lükatud.
        </p>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'BookingSentModal',
  props: {
    isOpen: { type: Boolean, default: false },
  },
  emits: ['event-modal-closed'],
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
.booking-modal-backdrop {
  position: fixed;
  z-index: 1055;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}

.booking-modal {
  width: min(100%, 560px);
}
</style>
