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
        <div class="form-text">
          Pilt vähendatakse automaatselt väikeseks eelvaateks (kuni {{ maxImageBytes }} baiti), sest
          andmebaas ei luba suuremat pilti salvestada.
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// Andmebaasi indeks (tool_id, image_data) mahutab ühe rea kohta ~2,7 KB, seega jääme sellest allapoole.
const MAX_IMAGE_BYTES = 2400
const IMAGE_SIZES = [128, 112, 96, 80, 72, 64, 56, 48, 40, 32, 24]
const IMAGE_QUALITIES = [0.7, 0.55, 0.4, 0.3, 0.2]
const IMAGE_SHRINK_FAILED = 'Pildi vähendamine ebaõnnestus. Proovi teist pilti.'

export default {
  name: 'ImagePicker',
  props: {
    disabled: { type: Boolean, default: false },
  },
  emits: ['event-image-selected', 'event-image-reading', 'event-image-error'],
  data() {
    return {
      maxImageBytes: MAX_IMAGE_BYTES,
      fileName: '',
      previewUrl: '',
      imageData: '',
      readGeneration: 0,
    }
  },
  beforeUnmount() {
    this.readGeneration += 1
    this.releasePreviewUrl()
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
        if (dataUrl.indexOf(',') < 0) {
          URL.revokeObjectURL(candidateUrl)
          this.$emit('event-image-error', 'Valitud faili lugemine ebaõnnestus. Proovi uuesti.')
          this.$emit('event-image-reading', false)
          return
        }

        this.shrinkImage(dataUrl)
          .then((shrunkDataUrl) => this.handleImageShrunk(generation, file.name, shrunkDataUrl))
          .catch(() => this.handleImageShrinkFailed(generation))
          .finally(() => URL.revokeObjectURL(candidateUrl))
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

    // Vähendab pildi JPEG-iks, mille suurus on kuni MAX_IMAGE_BYTES: proovib suurimat mõõtu ja kõrgeimat kvaliteeti.
    shrinkImage(dataUrl) {
      return this.loadImage(dataUrl).then((image) => {
        for (const size of IMAGE_SIZES) {
          for (const quality of IMAGE_QUALITIES) {
            const shrunkDataUrl = this.drawImage(image, size, quality)
            if (this.getDataUrlBytes(shrunkDataUrl) <= MAX_IMAGE_BYTES) return shrunkDataUrl
          }
        }
        throw new Error('Pilti ei saanud piisavalt väikeseks')
      })
    },

    loadImage(dataUrl) {
      return new Promise((resolve, reject) => {
        const image = new Image()
        image.onload = () => resolve(image)
        image.onerror = () => reject(new Error('Pildi lugemine ebaõnnestus'))
        image.src = dataUrl
      })
    },

    // Joonistab pildi canvasele nii, et pikim külg on maxSize pikslit, valge taustaga (JPEG ei toeta läbipaistvust).
    drawImage(image, maxSize, quality) {
      const scale = Math.min(1, maxSize / Math.max(image.width, image.height))
      const canvas = document.createElement('canvas')
      canvas.width = Math.max(1, Math.round(image.width * scale))
      canvas.height = Math.max(1, Math.round(image.height * scale))
      const context = canvas.getContext('2d')
      context.fillStyle = '#ffffff'
      context.fillRect(0, 0, canvas.width, canvas.height)
      context.drawImage(image, 0, 0, canvas.width, canvas.height)
      return canvas.toDataURL('image/jpeg', quality)
    },

    // Arvutab base64 andmete tegeliku baitide arvu (Base64 4 märki = 3 baiti, lõpu '=' märgid ei loe).
    getDataUrlBytes(dataUrl) {
      const base64 = dataUrl.slice(dataUrl.indexOf(',') + 1)
      const padding = base64.endsWith('==') ? 2 : base64.endsWith('=') ? 1 : 0
      return Math.floor((base64.length * 3) / 4) - padding
    },

    // Pilt on vähendatud ja valmis: eelvaade näitab täpselt seda, mis salvestatakse.
    handleImageShrunk(generation, fileName, shrunkDataUrl) {
      if (generation !== this.readGeneration) return
      this.releasePreviewUrl()
      this.previewUrl = shrunkDataUrl
      this.fileName = fileName
      this.imageData = shrunkDataUrl.slice(shrunkDataUrl.indexOf(',') + 1)
      this.$emit('event-image-selected', { fileName: this.fileName, imageData: this.imageData })
      this.$emit('event-image-reading', false)
    },

    handleImageShrinkFailed(generation) {
      if (generation !== this.readGeneration) return
      this.$emit('event-image-error', IMAGE_SHRINK_FAILED)
      this.$emit('event-image-reading', false)
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
