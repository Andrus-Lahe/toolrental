<template>
  <div v-if="isOpen" class="category-modal-backdrop" role="presentation" @click.self="cancel">
    <section
      class="category-modal card shadow"
      role="dialog"
      aria-modal="true"
      aria-labelledby="category-modal-title"
    >
      <div class="card-body">
        <h2 id="category-modal-title" class="h4 mb-3">{{ title }}</h2>
        <CategoryForm
          :category="category"
          :is-saving="isSaving"
          :error-message="errorMessage"
          @event-submit="$emit('event-save', $event)"
          @event-cancel="cancel"
        />
      </div>
    </section>
  </div>
</template>

<script>
import CategoryForm from '@/components/forms/CategoryForm.vue'

export default {
  name: 'CategoryModal',
  components: { CategoryForm },
  props: {
    isOpen: { type: Boolean, default: false },
    category: { type: Object, default: null },
    isSaving: { type: Boolean, default: false },
    errorMessage: { type: String, default: '' },
  },
  emits: ['event-save', 'event-cancel'],
  computed: {
    title() {
      return this.category ? 'Muuda kategooriat' : 'Lisa kategooria'
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
      if (event.key === 'Escape') this.cancel()
    },
    cancel() {
      if (!this.isSaving) this.$emit('event-cancel')
    },
  },
}
</script>

<style scoped>
.category-modal-backdrop {
  position: fixed;
  z-index: 1055;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}

.category-modal {
  width: min(100%, 520px);
}
</style>
