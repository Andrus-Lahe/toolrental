<template>
  <button type="button" class="category-card text-start" @click="$emit('event-category-selected', category.categoryId)">
    <div class="category-image">
      <img
        v-if="imageUrl && !imageFailed"
        :src="imageUrl"
        :alt="category.categoryName"
        @error="imageFailed = true"
      />
      <span v-else aria-hidden="true">Pilt puudub</span>
    </div>
    <div class="category-card-copy">
      <strong class="category-card-title">{{ category.categoryName }}</strong>
      <span class="category-card-description">{{ category.categoryDescription }}</span>
      <span class="category-card-arrow" aria-hidden="true">→</span>
    </div>
  </button>
</template>

<script>
export default {
  name: 'CategoryCard',
  props: {
    category: { type: Object, required: true },
  },
  emits: ['event-category-selected'],
  data() {
    return { imageFailed: false }
  },
  computed: {
    imageUrl() {
      const data = this.category.imageData
      if (!data || typeof data !== 'string') return ''
      if (data.startsWith('data:image/')) return data

      try {
        const prefix = atob(data.slice(0, 160)).trimStart()
        if (prefix.startsWith('<svg') || prefix.startsWith('<?xml')) {
          return `data:image/svg+xml;base64,${data}`
        }
        if (prefix.startsWith('\x89PNG')) return `data:image/png;base64,${data}`
        if (prefix.startsWith('\xff\xd8')) return `data:image/jpeg;base64,${data}`
        if (prefix.startsWith('GIF8')) return `data:image/gif;base64,${data}`
        if (prefix.startsWith('RIFF') && prefix.slice(8, 12) === 'WEBP') {
          return `data:image/webp;base64,${data}`
        }
      } catch {
        return ''
      }
      return ''
    },
  },
}
</script>

<style scoped>
.category-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  height: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  color: var(--color-ink);
  text-align: left;
  box-shadow: var(--shadow-card);
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
}

.category-card:hover,
.category-card:focus-visible {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.category-image {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: calc(var(--radius-card) - 0.3rem);
  background: var(--color-surface-soft);
  color: var(--color-muted);
}

.category-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-card-copy {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  flex: 1;
  min-width: 0;
  align-content: start;
  gap: var(--space-2) var(--space-3);
  padding: var(--space-3) var(--space-2) var(--space-2);
}

.category-card-title {
  grid-column: 1;
  font-size: 1.05rem;
  line-height: 1.35;
}

.category-card-description {
  grid-column: 1;
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  overflow-wrap: anywhere;
  color: var(--color-muted);
  font-size: var(--font-size-small);
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.category-card-arrow {
  grid-column: 2;
  grid-row: 1 / span 2;
  align-self: center;
  color: var(--color-primary);
  font-size: 1.3rem;
  transition: transform 150ms ease;
}

.category-card:hover .category-card-arrow,
.category-card:focus-visible .category-card-arrow {
  transform: translateX(3px);
}

@media (prefers-reduced-motion: reduce) {
  .category-card,
  .category-card-arrow {
    transition: none;
  }
}
</style>
