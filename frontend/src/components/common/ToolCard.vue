<template>
  <article class="card h-100">
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

.tool-card-description {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  color: #475569;
  font-size: 0.875rem;
}
</style>
