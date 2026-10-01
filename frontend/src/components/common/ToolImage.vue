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
  overflow: hidden;
  background: #f1f5f9;
}

.tool-detail-image img,
.tool-detail-image-placeholder {
  display: grid;
  width: 100%;
  min-height: 260px;
  aspect-ratio: 4 / 3;
  place-items: center;
  object-fit: contain;
}

.tool-detail-image-placeholder {
  color: #64748b;
}
</style>
