<template>
  <main class="container py-4 py-lg-5">
    <h1 class="h2 fw-bold mb-4">Tööriista detailid</h1>
    <AlertDanger :error-message="errorMessage" />

    <p v-if="isLoading" class="py-4 text-center" role="status">Laen tööriista andmeid…</p>

    <section v-else-if="tool" class="row g-4">
      <div class="col-12 col-lg-7">
        <ToolImage :image-data="tool.imageData" :alt-text="tool.toolName || 'Tööriista pilt'" />

        <div class="mt-4">
          <h2 class="h3">{{ tool.toolName }}</h2>
          <p class="text-muted mb-3">{{ tool.categoryName }}</p>
          <p class="tool-description">{{ toolDescription || 'Kirjeldus puudub' }}</p>
          <div v-if="isToolUnavailable" class="alert alert-warning" role="status">
            Tööriist pole hetkel saadaval
          </div>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="isLendDisabled"
            @click="handleLendClick"
          >
            Laenuta
          </button>
          <p v-if="isOwnTool" class="form-text mb-0">
            See on sinu tööriist. Oma tööriista laenutada ei saa.
          </p>
        </div>
      </div>

      <aside v-if="isLoggedIn" class="col-12 col-lg-5">
        <AlertDanger :error-message="ownerErrorMessage" />
        <OwnerContactCard v-if="owner" :owner="owner" />
        <p v-else-if="isOwnerLoading" class="text-muted" role="status">
          Laen omaniku kontaktandmeid…
        </p>
      </aside>
    </section>
  </main>
</template>

<script>
import ToolService from '@/api-services/ToolService.js'
import UserService from '@/api-services/UserService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import OwnerContactCard from '@/components/common/OwnerContactCard.vue'
import ToolImage from '@/components/common/ToolImage.vue'
import { session } from '@/auth/session.js'
import NavigationService from '@/navigation/NavigationService.js'

const TOOL_LOAD_FAILED = 'Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const OWNER_LOAD_FAILED = 'Omaniku kontaktandmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'

export default {
  name: 'ToolDetailView',
  components: { AlertDanger, OwnerContactCard, ToolImage },
  inject: ['openLoginModal'],
  data() {
    return {
      session,
      tool: null,
      owner: null,
      errorMessage: '',
      ownerErrorMessage: '',
      isLoading: true,
      isOwnerLoading: false,
      ownerRequestStarted: false,
      loadGeneration: 0,
    }
  },
  computed: {
    toolId() {
      return String(this.$route.params.toolId ?? '')
    },
    isLendDisabled() {
      return this.isLoading || this.session.status === 'loading' || this.isOwnTool
    },
    isOwnTool() {
      return this.isLoggedIn && Number(this.session.user?.userId) === Number(this.tool?.ownerId)
    },
    isLoggedIn() {
      return this.session.status === 'authenticated'
    },
    isToolUnavailable() {
      return this.tool?.status === 'U'
    },
    toolDescription() {
      return this.tool?.description ?? ''
    },
  },
  watch: {
    '$route.params.toolId'() {
      this.loadTool()
    },
    'session.status'(status) {
      if (status === 'authenticated') this.loadOwnerIfReady()
      if (status === 'guest') {
        this.owner = null
        this.ownerErrorMessage = ''
        this.ownerRequestStarted = false
      }
    },
  },
  beforeMount() {
    this.loadTool()
  },
  beforeUnmount() {
    this.loadGeneration += 1
  },
  methods: {
    loadTool() {
      const generation = ++this.loadGeneration
      this.tool = null
      this.owner = null
      this.errorMessage = ''
      this.ownerErrorMessage = ''
      this.isLoading = true
      this.isOwnerLoading = false
      this.ownerRequestStarted = false

      return ToolService.sendGetToolDetailsRequest(this.toolId)
        .then((response) => this.handleToolResponse(response, generation))
        .catch((error) => this.handleToolError(error, generation))
        .finally(() => {
          if (generation === this.loadGeneration) this.isLoading = false
        })
    },

    handleToolResponse(response, generation) {
      if (generation !== this.loadGeneration) return
      const tool = response?.data
      if (response?.status !== 200 || !tool || !Number.isInteger(Number(tool.ownerId)) || Number(tool.ownerId) <= 0) {
        this.errorMessage = TOOL_LOAD_FAILED
        return
      }
      this.tool = tool
      this.loadOwnerIfReady()
    },

    loadOwnerIfReady() {
      if (!this.tool || !this.isLoggedIn || this.ownerRequestStarted) return
      this.ownerRequestStarted = true
      this.isOwnerLoading = true
      const generation = this.loadGeneration

      return UserService.sendGetUserDetailsRequest(this.tool.ownerId)
        .then((response) => this.handleOwnerResponse(response, generation))
        .catch((error) => this.handleOwnerError(error, generation))
        .finally(() => {
          if (generation === this.loadGeneration) this.isOwnerLoading = false
        })
    },

    handleOwnerResponse(response, generation) {
      if (generation !== this.loadGeneration) return
      const owner = response?.data
      if (response?.status !== 200 || !owner || Number(owner.userId) !== Number(this.tool?.ownerId)) {
        this.ownerErrorMessage = OWNER_LOAD_FAILED
        return
      }
      this.owner = owner
    },

    handleOwnerError(error, generation) {
      if (generation !== this.loadGeneration) return
      this.owner = null
      if (!error?.response) {
        this.ownerErrorMessage = NETWORK_ERROR_MESSAGE
        return
      }
      if (error.response.status === 401) {
        this.session.user = null
        this.session.status = 'guest'
        this.ownerErrorMessage = ''
        return
      }
      this.ownerErrorMessage = error.response.data?.message || OWNER_LOAD_FAILED
    },

    handleToolError(error, generation) {
      if (generation !== this.loadGeneration) return
      if (!error?.response) {
        this.errorMessage = NETWORK_ERROR_MESSAGE
        return
      }
      this.errorMessage = error.response.data?.message || TOOL_LOAD_FAILED
    },

    handleLendClick() {
      if (!this.tool || this.isLendDisabled) return
      if (this.isLoggedIn) {
        return NavigationService.navigateToBookingFormView(this.$router, this.toolId)
      }
      return this.openLoginModal()
    },
  },
}
</script>

<style scoped>
.tool-description {
  white-space: pre-line;
}
</style>
