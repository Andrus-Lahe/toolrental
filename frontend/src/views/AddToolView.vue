<template>
  <main class="container py-5">
    <section v-if="accessStatus === 'checking'" class="py-5 text-center" aria-live="polite">
      Kontrollin kasutaja ligipääsu…
    </section>

    <section v-else-if="accessStatus === 'guest'" class="mx-auto auth-message" role="alert">
      <h1>Logi sisse</h1>
      <p>Tööriista lisamiseks logi sisse Customer kontoga.</p>
      <button class="btn btn-primary" type="button" @click="openLoginModal">
        Logi sisse
      </button>
    </section>

    <section v-else-if="accessStatus === 'forbidden'" class="mx-auto auth-message" role="alert">
      <h1>Ligipääs puudub</h1>
      <p>Tööriista lisamine on võimalik Customer kontoga.</p>
    </section>

    <section v-else-if="accessStatus === 'error'" class="mx-auto auth-message" role="alert">
      <h1>Kasutaja kontrollimine ebaõnnestus</h1>
      <p>{{ accessError }}</p>
      <button class="btn btn-outline-primary" type="button" @click="checkAccess">
        Proovi uuesti
      </button>
    </section>

    <section v-else>
      <h1 class="h2 fw-bold mb-4">Lisa uus tööriist</h1>

      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <div v-if="categoryStatus === 'loading'" class="alert alert-info" role="status">
        Laen kategooriaid…
      </div>
      <div v-else-if="categoryStatus === 'error'" class="alert alert-danger" role="alert">
        {{ categoryError }}
        <button class="btn btn-link p-0 ms-2" type="button" @click="loadCategories">
          Proovi uuesti
        </button>
      </div>
      <div v-else-if="categories.length === 0" class="alert alert-warning" role="status">
        Kategooriaid pole saadaval. Tööriista ei saa praegu lisada.
      </div>

      <ToolCreateForm
        :categories="categories"
        :category-status="categoryStatus"
        :submitting="isSubmitting"
        :image-reading="isImageReading"
        :image-error="isImageReadFailed"
        @event-save="handleSave"
        @event-image-selected="handleImageSelected"
        @event-image-reading="isImageReading = $event"
        @event-image-error="handleImageError"
      />
    </section>
  </main>
</template>

<script>
import CategoryService from '@/api-services/CategoryService.js'
import ToolService from '@/api-services/ToolService.js'
import ToolCreateForm from '@/components/forms/ToolCreateForm.vue'
import NavigationService from '@/navigation/NavigationService.js'
import { loadSession, session } from '@/auth/session.js'

const CATEGORY_LOADING_FAILED = 'Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const TOOL_SAVE_FAILED = 'Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti.'

export default {
  name: 'AddToolView',
  components: { ToolCreateForm },
  inject: ['openLoginModal'],
  data() {
    return {
      accessStatus: 'checking',
      accessError: '',
      categories: [],
      categoryStatus: 'loading',
      categoryError: '',
      errorMessage: '',
      isSubmitting: false,
      isImageReading: false,
      isImageReadFailed: false,
      session,
    }
  },
  methods: {
    checkAccess() {
      this.accessStatus = 'checking'
      this.accessError = ''
      loadSession().then(() => this.handleSessionLoaded())
    },

    handleSessionLoaded() {
      if (this.session.status === 'error') {
        this.accessStatus = 'error'
        this.accessError = this.session.error
        return
      }
      if (this.session.status !== 'authenticated') {
        this.accessStatus = 'guest'
        return
      }
      if (this.session.user?.roleName !== 'customer') {
        this.accessStatus = 'forbidden'
        return
      }
      if (!this.session.user?.hasProfile) {
        NavigationService.navigateToProfile(this.$router)
        return
      }

      this.accessStatus = 'allowed'
      this.loadCategories()
    },

    loadCategories() {
      this.categoryStatus = 'loading'
      this.categoryError = ''
      CategoryService.sendGetCategoriesRequest()
        .then((response) => this.handleCategoriesLoaded(response.data))
        .catch((error) => this.handleCategoriesError(error))
        .finally(() => {
          if (this.categoryStatus === 'loading') this.categoryStatus = 'error'
        })
    },

    handleCategoriesLoaded(categories) {
      if (!Array.isArray(categories)) {
        this.handleCategoriesError(null)
        return
      }
      this.categories = categories
      this.categoryStatus = 'loaded'
    },

    handleCategoriesError(error) {
      this.categories = []
      this.categoryStatus = 'error'
      this.categoryError = error?.response?.data?.message ?? CATEGORY_LOADING_FAILED
    },

    handleSave(request) {
      if (this.isSubmitting || this.isImageReading) return
      this.errorMessage = ''
      this.isSubmitting = true
      ToolService.sendCreateToolRequest(request)
        .then(() => this.handleToolCreated())
        .catch((error) => this.handleSaveError(error))
        .finally(() => {
          this.isSubmitting = false
        })
    },

    handleToolCreated() {
      NavigationService.navigateToMyTools(this.$router, 'Tööriist lisatud')
    },

    handleSaveError(error) {
      const apiError = error?.response?.data
      const status = error?.response?.status
      if (apiError?.message) {
        this.errorMessage = apiError.message
      } else if (status === 401) {
        this.errorMessage = 'Sinu sisselogimine on aegunud. Logi sisse ja proovi uuesti.'
        this.openLoginModal()
      } else if (status === 403) {
        this.errorMessage = 'Sul puudub ligipääs või sessioon aegus. Kontrolli sisselogimist ja proovi uuesti.'
      } else if (!error?.response) {
        this.errorMessage = 'Serveriga ei saadud ühendust. Kontrolli ühendust ja proovi uuesti.'
      } else {
        this.errorMessage = TOOL_SAVE_FAILED
      }

      if (status === 403 && apiError?.errorCode === 'PROFILE_REQUIRED') {
        NavigationService.navigateToProfile(this.$router)
      } else if (status === 404 && apiError?.errorCode === 'PRIMARY_KEY_NOT_FOUND') {
        this.loadCategories()
      }
    },

    handleImageError(message) {
      this.isImageReadFailed = true
      this.errorMessage = message
    },

    handleImageSelected() {
      this.isImageReadFailed = false
      this.errorMessage = ''
    },
  },
  beforeMount() {
    this.checkAccess()
  },
}
</script>

<style scoped>
.auth-message {
  max-width: 520px;
}
</style>
