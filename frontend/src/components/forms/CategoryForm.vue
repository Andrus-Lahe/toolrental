<template>
  <form novalidate @submit.prevent="submit">
    <AlertDanger :error-message="errorMessage || validationMessage" />

    <div class="mb-3">
      <label for="categoryName" class="form-label">Nimi</label>
      <input
        id="categoryName"
        v-model="form.categoryName"
        type="text"
        class="form-control"
        maxlength="100"
      />
    </div>
    <div class="mb-3">
      <label for="categoryDescription" class="form-label">Kirjeldus (valikuline)</label>
      <textarea
        id="categoryDescription"
        v-model="form.description"
        class="form-control"
        rows="3"
        maxlength="255"
      ></textarea>
    </div>
    <div class="mb-4">
      <label for="categorySequence" class="form-label">Järjekord</label>
      <input
        id="categorySequence"
        v-model="form.sequence"
        type="number"
        step="1"
        class="form-control"
      />
    </div>

    <div class="d-flex justify-content-end gap-2">
      <button type="button" class="btn btn-outline-secondary" :disabled="isSaving" @click="cancel">
        Tühista
      </button>
      <button type="submit" class="btn btn-primary" :disabled="isSaving">Salvesta</button>
    </div>
  </form>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'

const NAME_MAX_LENGTH = 100
const DESCRIPTION_MAX_LENGTH = 255
const INTEGER_MIN = -2147483648
const INTEGER_MAX = 2147483647

export default {
  name: 'CategoryForm',
  components: { AlertDanger },
  props: {
    category: {
      type: Object,
      default: null,
    },
    isSaving: {
      type: Boolean,
      default: false,
    },
    errorMessage: {
      type: String,
      default: '',
    },
  },
  emits: ['event-submit', 'event-cancel'],
  data() {
    return {
      validationMessage: '',
      // Vormi väärtused kopeeritakse eraldi, et tabelirida ei muutuks enne serveri vastust.
      form: {
        categoryName: this.category?.categoryName ?? '',
        description: this.category?.description ?? '',
        sequence: this.category ? String(this.category.sequence) : '',
      },
    }
  },
  methods: {
    submit() {
      if (this.isSaving) return
      this.validationMessage = this.validate()
      if (this.validationMessage === '') {
        this.$emit('event-submit', this.createCategoryRequest())
      }
    },

    // Tagastab esimese vea teksti või tühja sõne, kui vorm on korrektne. Arv 0 ja negatiivsed täisarvud on lubatud.
    validate() {
      const categoryName = this.form.categoryName.trim()
      if (categoryName === '') return 'Nimi ei tohi olla tühi'
      if (categoryName.length > NAME_MAX_LENGTH) {
        return `Nimi ei tohi olla pikem kui ${NAME_MAX_LENGTH} märki`
      }
      if (this.form.description.length > DESCRIPTION_MAX_LENGTH) {
        return `Kirjeldus ei tohi olla pikem kui ${DESCRIPTION_MAX_LENGTH} märki`
      }
      const sequence = String(this.form.sequence).trim()
      if (!/^-?\d+$/.test(sequence)) return 'Järjekord peab olema täisarv'
      if (Number(sequence) < INTEGER_MIN || Number(sequence) > INTEGER_MAX) {
        return 'Järjekord on liiga suur või liiga väike'
      }
      return ''
    },

    // Päringu keha: sequence saadetakse arvuna ja tühi kirjeldus kujul null.
    createCategoryRequest() {
      return {
        categoryName: this.form.categoryName.trim(),
        description: this.form.description.trim() || null,
        sequence: Number(String(this.form.sequence).trim()),
      }
    },

    cancel() {
      if (!this.isSaving) this.$emit('event-cancel')
    },
  },
}
</script>
