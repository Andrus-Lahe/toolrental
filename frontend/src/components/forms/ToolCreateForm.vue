<template>
  <form class="tool-create-form" novalidate @submit.prevent="handleSubmit">
    <ImagePicker
      :disabled="submitting"
      @event-image-selected="handleImageSelected"
      @event-image-reading="$emit('event-image-reading', $event)"
      @event-image-error="$emit('event-image-error', $event)"
    />

    <div class="mt-4">
      <label class="form-label" for="tool-name">Tööriista nimi</label>
      <input
        id="tool-name"
        v-model="name"
        class="form-control"
        type="text"
        maxlength="150"
        required
        :disabled="submitting"
        :aria-invalid="Boolean(nameError)"
        @input="nameError = ''"
      />
      <div v-if="nameError" class="form-text text-danger" role="alert">{{ nameError }}</div>
      <div class="form-text">{{ name.length }}/150</div>
    </div>

    <div class="mt-3">
      <label class="form-label" for="tool-description">Kirjeldus</label>
      <textarea
        id="tool-description"
        v-model="description"
        class="form-control"
        rows="4"
        maxlength="2000"
        :disabled="submitting"
      ></textarea>
      <div class="form-text">{{ description.length }}/2000</div>
      <div v-if="descriptionError" class="form-text text-danger" role="alert">
        {{ descriptionError }}
      </div>
    </div>

    <div class="mt-3">
      <label class="form-label" for="tool-category">Kategooria</label>
      <select
        id="tool-category"
        v-model="categoryId"
        class="form-select"
        required
        :disabled="submitting || categoryStatus !== 'loaded' || categories.length === 0"
        :aria-invalid="Boolean(categoryError)"
        @change="categoryError = ''"
      >
        <option value="">{{ categories.length ? 'Vali kategooria' : 'Kategooriaid pole' }}</option>
        <option v-for="category in categories" :key="category.categoryId" :value="String(category.categoryId)">
          {{ category.categoryName }}
        </option>
      </select>
      <div v-if="categoryError" class="form-text text-danger" role="alert">
        {{ categoryError }}
      </div>
    </div>

    <div class="d-flex justify-content-end mt-5">
      <button class="btn btn-primary px-4" type="submit" :disabled="!canSave">
        {{ submitting ? 'Salvestan…' : 'Salvesta' }}
      </button>
    </div>
  </form>
</template>

<script>
import ImagePicker from '@/components/common/ImagePicker.vue'

export default {
  name: 'ToolCreateForm',
  components: { ImagePicker },
  props: {
    categories: { type: Array, default: () => [] },
    categoryStatus: { type: String, default: 'loading' },
    submitting: { type: Boolean, default: false },
    imageReading: { type: Boolean, default: false },
    imageError: { type: Boolean, default: false },
  },
  emits: ['event-save', 'event-image-selected', 'event-image-reading', 'event-image-error'],
  data() {
    return {
      name: '',
      description: '',
      categoryId: '',
      imageData: '',
      nameError: '',
      descriptionError: '',
      categoryError: '',
    }
  },
  computed: {

    canSave() {
      return this.categoryStatus === 'loaded'
        && this.hasValidCategory
        && !this.submitting
        && !this.imageReading
        && !this.imageError
    },
    hasValidCategory() {
      return this.categories.some((category) => String(category.categoryId) === this.categoryId)
    },
  },
  methods: {
    handleImageSelected(image) {
      this.imageData = image.imageData
      this.$emit('event-image-selected', image)
    },

    handleSubmit() {
      this.nameError = ''
      this.descriptionError = ''
      this.categoryError = ''

      if (!this.name.trim()) this.nameError = 'name: ei tohi olla tühi'
      else if (this.name.length > 150) this.nameError = 'name: ei tohi ületada 150 märki'

      if (this.description.length > 2000) {
        this.descriptionError = 'description: ei tohi ületada 2000 märki'
      }

      if (!this.hasValidCategory || Number(this.categoryId) <= 0) {
        this.categoryError = 'categoryId: vali olemasolev kategooria'
      }

      if (this.nameError || this.descriptionError || this.categoryError || !this.canSave) return

      this.$emit('event-save', {
        categoryId: Number(this.categoryId),
        name: this.name.trim(),
        description: this.description.trim() ? this.description : null,
        imageData: this.imageData,
      })
    },
  },
}
</script>

<style scoped>
.tool-create-form {
  max-width: 760px;
}
</style>
