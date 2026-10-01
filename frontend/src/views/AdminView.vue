<template>
  <main class="container py-5">
    <section v-if="accessStatus === 'checking'" class="py-5 text-center" aria-live="polite">
      Kontrollin kasutaja ligipääsu…
    </section>

    <section v-else-if="accessStatus === 'guest'" class="mx-auto auth-message" role="alert">
      <h1>Logi sisse</h1>
      <p>Halduse avamiseks logi sisse admini kontoga.</p>
      <button class="btn btn-primary" type="button" @click="openLoginModal">Logi sisse</button>
    </section>

    <section v-else-if="accessStatus === 'forbidden'" class="mx-auto auth-message" role="alert">
      <h1>Ligipääs puudub</h1>
      <p>Haldus on saadaval ainult adminile.</p>
    </section>

    <section v-else-if="accessStatus === 'error'" class="mx-auto auth-message" role="alert">
      <h1>Kasutaja kontrollimine ebaõnnestus</h1>
      <p>{{ accessError }}</p>
      <button class="btn btn-outline-primary" type="button" @click="checkAccess">
        Proovi uuesti
      </button>
    </section>

    <section v-else aria-labelledby="admin-title">
      <h1 id="admin-title" class="mb-4">Haldus</h1>

      <h2 class="h3 mb-3">Kasutajad</h2>
      <AlertDanger :error-message="errorMessage" />
      <div v-if="reloadFailed" class="alert alert-warning" role="alert">
        Muudatus õnnestus, kuid kasutajate loendi värskendamine ebaõnnestus.
        <button type="button" class="btn btn-link p-0 ms-2" @click="getUsers">Proovi uuesti</button>
      </div>

      <p v-if="isLoading" role="status">Kasutajate laadimine...</p>
      <p v-else-if="users.length === 0 && !loadFailed">Kasutajaid ei ole</p>
      <AdminUsersTable
        v-else-if="users.length > 0"
        :users="users"
        :pending-user-id="pendingUserId"
        @event-block-user="handleBlockUser"
        @event-delete-user="handleDeleteRequested"
      />
      <button
        v-if="loadFailed && !isLoading"
        type="button"
        class="btn btn-outline-primary"
        @click="getUsers"
      >
        Proovi uuesti
      </button>

      <h2 class="h3 mt-5 mb-3">Kategooriad</h2>
      <AlertDanger :error-message="categoryErrorMessage" />
      <div v-if="categoriesReloadFailed" class="alert alert-warning" role="alert">
        Muudatus õnnestus, kuid kategooriate loendi värskendamine ebaõnnestus.
        <button type="button" class="btn btn-link p-0 ms-2" @click="getCategories">
          Proovi uuesti
        </button>
      </div>

      <button type="button" class="btn btn-outline-primary mb-3" @click="handleAddCategory">
        Lisa kategooria
      </button>

      <p v-if="isCategoriesLoading" role="status">Kategooriate laadimine...</p>
      <p v-else-if="categories.length === 0 && !categoriesLoadFailed">Kategooriaid ei ole</p>
      <AdminCategoriesTable
        v-else-if="categories.length > 0"
        :categories="categories"
        :pending-category-id="pendingCategoryId"
        @event-edit-category="handleEditCategory"
        @event-delete-category="handleCategoryDeleteRequested"
      />
      <button
        v-if="categoriesLoadFailed && !isCategoriesLoading"
        type="button"
        class="btn btn-outline-primary"
        @click="getCategories"
      >
        Proovi uuesti
      </button>
    </section>

    <ConfirmDeleteModal
      :is-open="userToDelete !== null"
      title="Kustuta kasutaja"
      :message="deleteMessage"
      :is-busy="pendingUserId !== 0"
      @event-confirm="handleDeleteConfirmed"
      @event-cancel="handleDeleteCancelled"
    />
    <ConfirmDeleteModal
      :is-open="categoryToDelete !== null"
      title="Kustuta kategooria"
      :message="categoryDeleteMessage"
      :is-busy="pendingCategoryId !== 0"
      @event-confirm="handleCategoryDeleteConfirmed"
      @event-cancel="handleCategoryDeleteCancelled"
    />
    <CategoryModal
      :is-open="isCategoryModalOpen"
      :category="categoryToEdit"
      :is-saving="isCategorySaving"
      :error-message="categoryModalError"
      @event-save="handleCategorySave"
      @event-cancel="handleCategoryModalCancelled"
    />
  </main>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import AdminUsersTable from '@/components/tables/AdminUsersTable.vue'
import AdminCategoriesTable from '@/components/tables/AdminCategoriesTable.vue'
import ConfirmDeleteModal from '@/components/modals/ConfirmDeleteModal.vue'
import CategoryModal from '@/components/modals/CategoryModal.vue'
import AdminUserService from '@/api-services/AdminUserService.js'
import AdminCategoryService from '@/api-services/AdminCategoryService.js'
import { loadSession, session } from '@/auth/session.js'

const CATEGORIES_LOADING_FAILED = 'Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const USERS_LOADING_FAILED = 'Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
const ACCESS_DENIED_MESSAGE = 'Ligipääs puudub.'

export default {
  name: 'AdminView',
  components: {
    AlertDanger,
    AdminUsersTable,
    AdminCategoriesTable,
    ConfirmDeleteModal,
    CategoryModal,
  },
  inject: ['openLoginModal'],
  data() {
    return {
      accessStatus: 'checking',
      accessError: '',
      users: [],
      isLoading: false,
      loadFailed: false,
      reloadFailed: false,
      errorMessage: '',
      pendingUserId: 0,
      userToDelete: null,
      categories: [],
      isCategoriesLoading: false,
      categoriesLoadFailed: false,
      categoriesReloadFailed: false,
      categoryErrorMessage: '',
      pendingCategoryId: 0,
      categoryToDelete: null,
      isCategoryModalOpen: false,
      categoryToEdit: null,
      isCategorySaving: false,
      categoryModalError: '',
      session,
    }
  },
  computed: {
    deleteMessage() {
      if (!this.userToDelete) return ''
      return `Kas oled kindel, et soovid kasutaja ${this.userToDelete.name} kustutada?`
    },

    categoryDeleteMessage() {
      if (!this.categoryToDelete) return ''
      return `Kas oled kindel, et soovid kategooria ${this.categoryToDelete.categoryName} kustutada?`
    },
  },
  beforeMount() {
    this.checkAccess()
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
      } else if (this.session.status !== 'authenticated') {
        this.accessStatus = 'guest'
      } else if (this.session.user?.roleName !== 'admin') {
        this.accessStatus = 'forbidden'
      } else {
        this.accessStatus = 'allowed'
        this.getUsers()
        this.getCategories()
      }
    },

    getUsers() {
      this.isLoading = true
      this.loadFailed = false
      this.reloadFailed = false
      AdminUserService.sendGetAdminUsersRequest()
        .then((response) => this.handleGetUsersResponse(response.data))
        .catch((error) => this.handleGetUsersError(error))
        .finally(() => (this.isLoading = false))
    },

    // Loendi laadimise vea korral jäävad varem laetud andmed nähtavale.
    handleGetUsersError(error) {
      this.loadFailed = true
      this.handleApiError(error, USERS_LOADING_FAILED)
    },

    getCategories() {
      this.isCategoriesLoading = true
      this.categoriesLoadFailed = false
      this.categoriesReloadFailed = false
      AdminCategoryService.sendGetAdminCategoriesRequest()
        .then((response) => this.handleGetCategoriesResponse(response.data))
        .catch((error) => this.handleGetCategoriesError(error))
        .finally(() => (this.isCategoriesLoading = false))
    },

    // Loendi laadimise vea korral jäävad varem laetud andmed nähtavale.
    handleGetCategoriesError(error) {
      this.categoriesLoadFailed = true
      this.categoryErrorMessage = this.createCategoryErrorMessage(error, CATEGORIES_LOADING_FAILED)
    },

    handleBlockUser(userId) {
      if (this.pendingUserId !== 0) return
      this.errorMessage = ''
      this.pendingUserId = userId
      AdminUserService.sendPatchUserStatusRequest(userId, 'B')
        .then(() => this.reloadUsersAfterChange())
        .catch((error) => this.handleApiError(error))
        .finally(() => (this.pendingUserId = 0))
    },

    handleDeleteRequested(user) {
      if (this.pendingUserId !== 0) return
      const name = [user.firstName, user.lastName]
        .map((part) => (part ?? '').trim())
        .filter((part) => part !== '')
        .join(' ')
      this.userToDelete = { userId: user.userId, name }
    },

    handleDeleteCancelled() {
      this.userToDelete = null
    },

    handleDeleteConfirmed() {
      if (!this.userToDelete || this.pendingUserId !== 0) return
      this.errorMessage = ''
      const userId = this.userToDelete.userId
      this.pendingUserId = userId
      AdminUserService.sendDeleteUserRequest(userId)
        .then(() => this.reloadUsersAfterChange())
        .catch((error) => this.handleApiError(error))
        .finally(() => this.finishDelete())
    },

    finishDelete() {
      this.pendingUserId = 0
      this.userToDelete = null
    },

    handleAddCategory() {
      if (this.isCategorySaving) return
      this.categoryToEdit = null
      this.categoryModalError = ''
      this.isCategoryModalOpen = true
    },

    handleEditCategory(category) {
      if (this.pendingCategoryId !== 0 || this.isCategorySaving) return
      this.categoryToEdit = category
      this.categoryModalError = ''
      this.isCategoryModalOpen = true
    },

    handleCategoryModalCancelled() {
      if (this.isCategorySaving) return
      this.isCategoryModalOpen = false
    },

    // Lisab (POST) või muudab (PUT) sõltuvalt sellest, kas modaalis on muudetav kategooria.
    handleCategorySave(categoryRequest) {
      if (this.isCategorySaving) return
      this.categoryModalError = ''
      this.categoryErrorMessage = ''
      this.isCategorySaving = true
      const request = this.categoryToEdit
        ? AdminCategoryService.sendPutCategoryRequest(
            this.categoryToEdit.categoryId,
            categoryRequest,
          )
        : AdminCategoryService.sendPostCategoryRequest(categoryRequest)
      request
        .then(() => this.handleCategorySaved())
        .catch((error) => {
          this.categoryModalError = this.createCategoryErrorMessage(error)
        })
        .finally(() => (this.isCategorySaving = false))
    },

    // Salvestamine õnnestus: modaal suletakse ja loend laaditakse uuesti.
    handleCategorySaved() {
      this.isCategoryModalOpen = false
      return this.reloadCategoriesAfterChange()
    },

    handleCategoryDeleteRequested(category) {
      if (this.pendingCategoryId !== 0) return
      this.categoryToDelete = {
        categoryId: category.categoryId,
        categoryName: category.categoryName,
      }
    },

    handleCategoryDeleteCancelled() {
      this.categoryToDelete = null
    },

    handleCategoryDeleteConfirmed() {
      if (!this.categoryToDelete || this.pendingCategoryId !== 0) return
      this.categoryErrorMessage = ''
      const categoryId = this.categoryToDelete.categoryId
      this.pendingCategoryId = categoryId
      AdminCategoryService.sendDeleteCategoryRequest(categoryId)
        .then(() => this.reloadCategoriesAfterChange())
        .catch((error) => {
          this.categoryErrorMessage = this.createCategoryErrorMessage(error)
        })
        .finally(() => this.finishCategoryDelete())
    },

    finishCategoryDelete() {
      this.pendingCategoryId = 0
      this.categoryToDelete = null
    },

    // Muutmine õnnestus; ainult loendi värskendamise tõrge ei tohi väita, et muutmine ebaõnnestus.
    reloadUsersAfterChange() {
      this.reloadFailed = false
      return AdminUserService.sendGetAdminUsersRequest()
        .then((response) => this.handleGetUsersResponse(response.data))
        .catch(() => (this.reloadFailed = true))
    },

    handleGetUsersResponse(users) {
      this.users = Array.isArray(users) ? users : []
    },

    // Muutmine õnnestus; ainult loendi värskendamise tõrge ei tohi väita, et muutmine ebaõnnestus.
    reloadCategoriesAfterChange() {
      this.categoriesReloadFailed = false
      return AdminCategoryService.sendGetAdminCategoriesRequest()
        .then((response) => this.handleGetCategoriesResponse(response.data))
        .catch(() => (this.categoriesReloadFailed = true))
    },

    handleGetCategoriesResponse(categories) {
      this.categories = Array.isArray(categories) ? categories : []
    },

    // Tagastab kasutajale näidatava teksti; 401 ja tühja body'ga 403 muudavad vaate ligipääsu olekut.
    createCategoryErrorMessage(error, fallbackMessage = NETWORK_ERROR_MESSAGE) {
      const response = error.response
      if (!response) return NETWORK_ERROR_MESSAGE
      if (response.status === 401) {
        session.user = null
        session.status = 'guest'
        this.accessStatus = 'guest'
        return ''
      }
      if (response.status === 403 && !response.data?.message) {
        this.accessStatus = 'forbidden'
        return ACCESS_DENIED_MESSAGE
      }
      return response.data?.message ?? fallbackMessage
    },

    handleApiError(error, fallbackMessage = NETWORK_ERROR_MESSAGE) {
      const response = error.response
      if (!response) {
        this.errorMessage = NETWORK_ERROR_MESSAGE
      } else if (response.status === 401) {
        session.user = null
        session.status = 'guest'
        this.accessStatus = 'guest'
      } else if (response.status === 403 && !response.data?.message) {
        this.accessStatus = 'forbidden'
        this.errorMessage = ACCESS_DENIED_MESSAGE
      } else {
        this.errorMessage = response.data?.message ?? fallbackMessage
      }
    },
  },
}
</script>

<style scoped>
.auth-message {
  max-width: 520px;
}
</style>
