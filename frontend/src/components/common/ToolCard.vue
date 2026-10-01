<template>
  <article class="card tool-list-card h-100">
    <div class="tool-card-image">
      <img
        v-if="imageUrl && !imageFailed"
        :src="imageUrl"
        :alt="tool.toolName"
        class="card-img-top"
        @error="handleImageError"
      />
      <div
        v-else
        class="tool-card-placeholder"
        role="img"
        :aria-label="`${tool.toolName}: pilt puudub`"
      >
        <span aria-hidden="true">Pilt puudub</span>
      </div>
    </div>

    <div class="card-body d-flex flex-column">
      <h3 class="card-title h6">{{ tool.toolName }}</h3>
      <p v-if="tool.description" class="card-text tool-card-description">{{ tool.description }}</p>
      <button
        type="button"
        class="btn btn-outline-primary btn-sm mt-auto"
        @click="$emit('event-view-details', tool.toolId)"
      >
        Vaata detaile
      </button>
    </div>
  </article>
</template>

<script>
import { toImageDataUrl } from '@/utils/imageDataUrl'

export default {
  name: 'ToolCard',
  props: {
    tool: {
      type: Object,
      required: true,
    },
  },
  emits: ['event-view-details'],
  data() {
    return {
      imageFailed: false,
    }
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
  margin-bottom: var(--space-2);
  color: var(--color-ink);
  font-size: 1.05rem;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.tool-list-card .btn {
  width: 100%;
}

.tool-card-description {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  color: var(--color-muted);
  font-size: var(--font-size-small);
  line-height: 1.5;
}

@media (prefers-reduced-motion: reduce) {
  .tool-list-card {
    transition: none;
  }
}
</style>
