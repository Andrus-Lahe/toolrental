<template>
  <div v-if="isOpen" class="confirm-modal-backdrop" role="presentation" @click.self="cancel">
    <section
      class="confirm-modal card shadow"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-delete-title"
    >
      <div class="card-body">
        <h2 id="confirm-delete-title" class="h4">{{ title }}</h2>
        <p>{{ message }}</p>
        <div class="d-flex justify-content-end gap-2">
          <button
            type="button"
            class="btn btn-outline-secondary"
            :disabled="isBusy"
            @click="cancel"
          >
            Tühista
          </button>
          <button type="button" class="btn btn-danger" :disabled="isBusy" @click="confirm">
            Kinnita
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'ConfirmDeleteModal',
  props: {
    isOpen: { type: Boolean, default: false },
    title: { type: String, default: 'Kinnita kustutamine' },
    message: { type: String, default: '' },
    isBusy: { type: Boolean, default: false },
  },
  emits: ['event-confirm', 'event-cancel'],
  watch: {
    isOpen(isOpen) {
      if (isOpen) window.addEventListener('keydown', this.handleKeydown)
      else window.removeEventListener('keydown', this.handleKeydown)
    },
  },
  methods: {
    confirm() {
      this.$emit('event-confirm')
    },

    cancel() {
      if (!this.isBusy) this.$emit('event-cancel')
    },

    handleKeydown(event) {
      if (event.key === 'Escape') this.cancel()
    },
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeydown)
  },
}
</script>

<style scoped>
.confirm-modal-backdrop {
  position: fixed;
  z-index: 1055;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}

.confirm-modal {
  width: min(100%, 460px);
}
</style>
