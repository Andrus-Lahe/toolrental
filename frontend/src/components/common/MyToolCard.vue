<template>
  <article class="card h-100">
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
.tool-card-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #f1f5f9;
}

.tool-card-image img {
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
  color: #64748b;
  font-size: 0.875rem;
}
</style>
