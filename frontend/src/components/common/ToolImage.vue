<template>
  <div class="tool-detail-image">
    <img
      v-if="imageSource && !imageFailed"
      :src="imageSource"
      :alt="altText"
      class="img-fluid"
      @error="handleImageError"
    />
    <div v-else class="tool-detail-image-placeholder" role="img" :aria-label="`${altText}: pilt puudub`">
      <span aria-hidden="true">Pilt puudub</span>
    </div>
  </div>
</template>

<script>
import { toImageDataUrl } from '@/utils/imageDataUrl'

export default {
  name: 'ToolImage',
  props: {
    imageData: { type: String, default: null },
    altText: { type: String, default: 'Tööriista pilt' },
  },
  data() {
    return { imageFailed: false }
  },
  computed: {
    imageSource() {
      return toImageDataUrl(this.imageData)
    },
  },
  watch: {
    imageData() {
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
.tool-detail-image {
  padding: var(--space-3);
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.tool-detail-image img,
.tool-detail-image-placeholder {
  display: grid;
  width: 100%;
  min-height: 260px;
  aspect-ratio: 4 / 3;
  place-items: center;
  border-radius: calc(var(--radius-card) - 0.3rem);
  background: var(--color-surface-soft);
  object-fit: contain;
}

.tool-detail-image-placeholder {
  color: var(--color-muted);
  font-size: var(--font-size-small);
}

@media (max-width: 575.98px) {
  .tool-detail-image img,
  .tool-detail-image-placeholder {
    min-height: 0;
  }
}
</style>
