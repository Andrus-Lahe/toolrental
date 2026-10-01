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
    <strong class="d-block mt-3">{{ category.categoryName }}</strong>
    <span class="d-block mt-2">{{ category.categoryDescription }}</span>
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
  width: 100%;
  padding: 1rem;
  border: 1px solid #ced4da;
  border-radius: 0.4rem;
  background: white;
  color: inherit;
}
.category-card:hover,
.category-card:focus-visible {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgb(13 110 253 / 15%);
}
.category-image {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #f8f9fa;
  color: #6c757d;
}
.category-image img { width: 100%; height: 100%; object-fit: contain; }
</style>
