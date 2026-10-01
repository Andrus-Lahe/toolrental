<template>
  <div class="image-picker">
    <div class="d-flex flex-wrap gap-2 mb-3">
      <label class="btn btn-outline-secondary mb-0" :class="{ disabled }">
        Lisa pilt
        <input
          class="visually-hidden"
          type="file"
          accept="image/*"
          :disabled="disabled"
          @change="handleFileSelected"
        />
      </label>
      <button
        v-if="fileName"
        class="btn btn-outline-secondary"
        type="button"
        :disabled="disabled"
        @click="clearImage"
      >
        Eemalda pilt
      </button>
    </div>

    <div class="row g-3 align-items-center">
      <div class="col-12 col-md-4">
        <div class="image-preview" aria-label="Valitud pildi eelvaade">
          <img v-if="previewUrl" :src="previewUrl" alt="Valitud tööriista pilt" />
          <span v-else class="text-muted">Pildi eelvaade</span>
        </div>
      </div>
      <div class="col-12 col-md-8">
        <label class="form-label" for="tool-image-name">Pildi nimi</label>
        <input id="tool-image-name" class="form-control" :value="fileName" readonly />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImagePicker',
  props: {
    disabled: { type: Boolean, default: false },
  },
  emits: ['event-image-selected', 'event-image-reading', 'event-image-error'],
  data() {
    return {
      fileName: '',
      previewUrl: '',
      imageData: '',
      readGeneration: 0,
    }
  },
  methods: {
    handleFileSelected(event) {
      const file = event.target.files?.[0]
      // Failivaliku sulgemine ei muuda juba valitud pilti.
      event.target.value = ''
      if (!file) return

      const generation = ++this.readGeneration
      const candidateUrl = URL.createObjectURL(file)
      const reader = new FileReader()
      this.$emit('event-image-reading', true)

      reader.onload = () => {
        if (generation !== this.readGeneration) {
          URL.revokeObjectURL(candidateUrl)
          return
        }

        const dataUrl = typeof reader.result === 'string' ? reader.result : ''
        const separatorIndex = dataUrl.indexOf(',')
        if (separatorIndex < 0) {
          URL.revokeObjectURL(candidateUrl)
          this.$emit('event-image-error', 'Valitud faili lugemine ebaõnnestus. Proovi uuesti.')
          this.$emit('event-image-reading', false)
          return
        }

        this.releasePreviewUrl()
        this.previewUrl = candidateUrl
        this.fileName = file.name
        this.imageData = dataUrl.slice(separatorIndex + 1)
        this.$emit('event-image-selected', { fileName: this.fileName, imageData: this.imageData })
        this.$emit('event-image-reading', false)
      }

      reader.onerror = () => {
        URL.revokeObjectURL(candidateUrl)
        if (generation !== this.readGeneration) return
        this.$emit('event-image-error', 'Valitud faili lugemine ebaõnnestus. Proovi uuesti.')
        this.$emit('event-image-reading', false)
      }

      try {
        reader.readAsDataURL(file)
      } catch {
        URL.revokeObjectURL(candidateUrl)
        if (generation === this.readGeneration) {
          this.$emit('event-image-error', 'Valitud faili lugemine ebaõnnestus. Proovi uuesti.')
          this.$emit('event-image-reading', false)
        }
      }
    },

    clearImage() {
      this.readGeneration += 1
      this.$emit('event-image-reading', false)
      this.releasePreviewUrl()
      this.fileName = ''
      this.imageData = ''
      this.$emit('event-image-selected', { fileName: '', imageData: '' })
    },

    releasePreviewUrl() {
      if (this.previewUrl) URL.revokeObjectURL(this.previewUrl)
      this.previewUrl = ''
    },
  },
  beforeUnmount() {
    this.readGeneration += 1
    this.releasePreviewUrl()
  },
}
</script>

<style scoped>
.image-preview {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 4 / 2;
  overflow: hidden;
  border: 1px solid #ced4da;
  border-radius: 0.375rem;
  background: #f8f9fa;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
