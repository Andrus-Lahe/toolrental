<template>
  <div
    v-if="isOpen"
    class="required-fields-modal-backdrop"
    role="presentation"
    @click.self="closeModal"
    @keydown.esc="closeModal"
  >
    <section
      class="required-fields-modal card shadow"
      role="dialog"
      aria-modal="true"
      aria-labelledby="required-fields-modal-title"
    >
      <div class="card-body">
        <button
          class="btn-close float-end"
          type="button"
          aria-label="Sulge"
          @click="closeModal"
        ></button>
        <h2 id="required-fields-modal-title" class="h4 pe-4">
          Jätkamiseks täida kohustuslikud väljad.
        </h2>
        <div class="d-flex justify-content-end">
          <button type="button" class="btn btn-primary" @click="closeModal">Sain aru</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'RequiredFieldsModal',
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
.required-fields-modal-backdrop {
  position: fixed;
  z-index: 1055;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}

.required-fields-modal {
  width: min(100%, 560px);
}
</style>
