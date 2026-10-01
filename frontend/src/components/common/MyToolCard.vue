<template>
  <article class="card tool-list-card h-100">
    <div class="tool-card-image">
      <img
        v-if="imageUrl && !imageFailed"
        :src="imageUrl"
        :alt="`${tool.toolName} pilt`"
        class="card-img-top"
        @error="handleImageError"
      />
      <div v-else class="tool-card-placeholder" role="img" :aria-label="`${tool.toolName}: pilt puudub`">
        <span aria-hidden="true">Pilt puudub</span>
      </div>
    </div>

    <div class="card-body d-flex flex-column">
      <h4 class="card-title h6">{{ tool.toolName }}</h4>
      <div class="d-grid gap-2 mt-auto">
        <button
          type="button"
          class="btn btn-outline-primary btn-sm"
          :disabled="isDetailsDisabled"
          @click="$emit('event-view-details', tool)"
        >
          Vaata detaile
        </button>
        <button
          v-if="isDeletable"
          type="button"
          class="btn btn-outline-danger btn-sm"
          :disabled="isDeleteDisabled"
          @click="$emit('event-delete-tool', tool)"
        >
          Kustuta tööriist
        </button>
      </div>
    </div>
  </article>
</template>

<script>
import { toImageDataUrl } from '@/utils/imageDataUrl'

export default {
  name: 'MyToolCard',
  props: {
    tool: { type: Object, required: true },
    isDetailsDisabled: { type: Boolean, default: false },
    isDeletable: { type: Boolean, default: false },
    isDeleteDisabled: { type: Boolean, default: false },
  },
  emits: ['event-view-details', 'event-delete-tool'],
  data() {
    return { imageFailed: false }
  },
  computed: {
    imageUrl() {
      return toImageDataUrl(this.tool.imageData)
    },
  },
  watch: {
    'tool.imageData'() {
      this.imageFailed = false
    },
  },
  methods: {
    handleImageError() {
      this.imageFailed = true
    },
  },
}
</script>

<style scoped>
.tool-list-card {
  min-width: 0;
  padding: var(--space-3);
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
}

.tool-list-card:hover {
  border-color: var(--color-border-strong);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.tool-card-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: calc(var(--radius-card) - 0.3rem);
  background: var(--color-surface-soft);
}

.tool-card-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tool-card-placeholder {
  display: grid;
  width: 100%;
  height: 100%;
  min-height: 140px;
  place-items: center;
  color: var(--color-muted);
  font-size: var(--font-size-small);
}

.tool-list-card .card-body {
  min-width: 0;
  padding: var(--space-4) var(--space-2) var(--space-2);
}

.tool-list-card .card-title {
  margin-bottom: var(--space-4);
  color: var(--color-ink);
  font-size: 1.05rem;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.tool-list-card .btn {
  width: 100%;
}

@media (prefers-reduced-motion: reduce) {
  .tool-list-card {
    transition: none;
  }
}
</style>
