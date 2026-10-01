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
      <button
        type="button"
        class="btn btn-outline-primary btn-sm mt-auto"
        :disabled="isDetailsDisabled"
        @click="$emit('event-view-details', tool)"
      >
        Vaata detaile
      </button>
    </div>
  </article>
</template>

<script>
export default {
  name: 'MyToolCard',
  props: {
    tool: { type: Object, required: true },
    isDetailsDisabled: { type: Boolean, default: false },
  },
  emits: ['event-view-details'],
  data() {
    return { imageFailed: false }
  },
  computed: {
    imageUrl() {
      const imageData = this.tool.imageData?.trim()
      return imageData ? `data:image/svg+xml;base64,${imageData}` : ''
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
