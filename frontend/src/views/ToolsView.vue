<template>
  <main class="tools container py-4 py-lg-5">
    <header class="tools-heading mb-4 mb-lg-5">
      <p class="tools-eyebrow">Tööriistad sinu lähedal</p>
      <h1 class="mb-0">Leia sobiv tööriist</h1>
      <p class="tools-intro mb-0">Sirvi valikut ja täpsusta otsingut kategooria või asukoha järgi.</p>
    </header>

    <div class="row g-4 g-xl-5">
      <aside class="col-12 col-lg-3">
        <ToolsFilterForm
          :categories="categories"
          :cities="cities"
          :districts="districts"
          :selected-category-id="formFilters.categoryId"
          :selected-city-id="formFilters.cityId"
          :selected-district-id="formFilters.districtId"
          :is-categories-loaded="isCategoriesLoaded"
          :is-districts-loading="isDistrictsLoading"
          :is-districts-unavailable="Boolean(districtsErrorMessage)"
          @event-category-changed="handleCategoryChanged"
          @event-city-changed="handleCityChanged"
          @event-district-changed="handleDistrictChanged"
          @event-apply-filters="handleApplyFilters"
          @event-reset-filters="handleResetFilters"
        />

        <div class="tools-filter-status mt-3">
          <p v-if="isCategoriesLoading" class="small mb-2" role="status">Kategooriate laadimine…</p>
          <div v-if="categoriesErrorMessage" class="mb-2">
            <AlertDanger :error-message="categoriesErrorMessage" />
            <button type="button" class="btn btn-outline-primary btn-sm" @click="getCategories">
              Proovi uuesti
            </button>
          </div>

          <p v-if="isCitiesLoading" class="small mb-2" role="status">Linnade laadimine…</p>
          <div v-if="citiesErrorMessage" class="mb-2">
            <AlertDanger :error-message="citiesErrorMessage" />
            <button type="button" class="btn btn-outline-primary btn-sm" @click="getCities">
              Proovi uuesti
            </button>
          </div>

          <div v-if="districtsErrorMessage" class="mb-2">
            <AlertDanger :error-message="districtsErrorMessage" />
            <button
              type="button"
              class="btn btn-outline-primary btn-sm"
              @click="getDistricts(formFilters.cityId)"
            >
              Proovi uuesti
            </button>
          </div>
        </div>
      </aside>

      <section class="col-12 col-lg-9" aria-labelledby="tools-results-title">
        <h2 id="tools-results-title" class="tools-results-title h4 mb-4">Otsingu tulemused</h2>

        <div v-if="urlErrorMessage" class="mb-4">
          <AlertDanger :error-message="urlErrorMessage" />
          <button type="button" class="btn btn-outline-primary" @click="handleResetFilters">
            Lähtesta otsing
          </button>
        </div>

        <template v-else>
          <div v-if="toolsErrorMessage" class="mb-4">
            <AlertDanger :error-message="toolsErrorMessage" />
            <button
              type="button"
              class="btn btn-outline-primary"
              :disabled="isToolsLoading"
              @click="getTools"
            >
              Proovi uuesti
            </button>
          </div>

          <div v-if="isToolsResultStale" class="alert alert-warning" role="status">
            Kuvatud tulemus võib olla aegunud.
          </div>

          <p v-if="isToolsLoading" class="tools-feedback" role="status">Tööriistade laadimine…</p>

          <template v-if="toolsResponse">
            <p v-if="isEmptySearch" class="tools-feedback">Otsingule vastavaid tööriistu ei leitud.</p>

            <div v-else-if="isPageBeyondLast" class="tools-feedback">
              <p>Sellel lehel tööriistu pole.</p>
              <button type="button" class="btn btn-outline-primary" @click="handleGoToLastPage">
                Mine viimasele lehele
              </button>
            </div>

            <div v-else class="tool-grid mb-4">
              <ToolCard
                v-for="tool in toolsResponse.tools"
                :key="tool.toolId"
                :tool="tool"
                @event-view-details="handleViewDetails"
              />
            </div>

            <ToolsPagination
              v-if="toolsResponse.totalPages > 0"
              :page-number="appliedFilters.pageNumber"
              :total-pages="toolsResponse.totalPages"
              :is-disabled="isToolsLoading"
              @event-page-changed="handlePageChanged"
            />
          </template>
        </template>
      </section>
    </div>
  </main>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import ToolsPagination from '@/components/common/ToolsPagination.vue'
import ToolCard from '@/components/common/ToolCard.vue'
import ToolsFilterForm from '@/components/forms/ToolsFilterForm.vue'
import CategoryService from '@/api-services/CategoryService.js'
import CityService from '@/api-services/CityService.js'
import ToolService from '@/api-services/ToolService.js'
import NavigationService from '@/navigation/NavigationService.js'

const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
const TOOLS_LOADING_FAILED = 'Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const CATEGORIES_LOADING_FAILED = 'Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const CITIES_LOADING_FAILED = 'Linnade laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const DISTRICTS_LOADING_FAILED = 'Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
const MAX_INTEGER = 2147483647
const DEFAULT_FILTERS = { categoryId: 0, cityId: 0, districtId: 0, pageNumber: 1 }

export default {
  name: 'ToolsView',
  components: { AlertDanger, ToolCard, ToolsFilterForm, ToolsPagination },
  data() {
    return {
      categories: [],
      cities: [],
      districts: [],
      districtsCityId: 0,

      isCategoriesLoading: false,
      isCategoriesLoaded: false,
      isCitiesLoading: false,
      isDistrictsLoading: false,
      isToolsLoading: false,

      categoriesErrorMessage: '',
      citiesErrorMessage: '',
      districtsErrorMessage: '',
      toolsErrorMessage: '',
      urlErrorMessage: '',

      formFilters: { categoryId: 0, cityId: 0, districtId: 0 },
      appliedFilters: { ...DEFAULT_FILTERS },
      isFirstLoad: true,

      toolsResponse: null,
      isToolsResultStale: false,
      toolsRequestId: 0,
      districtsRequestId: 0,
    }
  },
  computed: {
    isEmptySearch() {
      return this.toolsResponse.totalElements === 0
    },
    isPageBeyondLast() {
      return this.toolsResponse.tools.length === 0 && this.toolsResponse.totalElements > 0
    },
  },
  watch: {
    '$route.query'() {
      if (this.$route.name === 'toolsRoute') {
        this.loadToolsFromRoute()
      }
    },
  },
  methods: {
    loadToolsFromRoute() {
      const { filters, errorMessage } = this.parseRouteQuery(this.$route.query)
      if (errorMessage) {
        this.toolsRequestId++
        this.isToolsLoading = false
        this.urlErrorMessage = errorMessage
        return
      }
      this.urlErrorMessage = ''
      const isFilterChanged = this.isFirstLoad || this.isFilterChanged(filters)
      this.appliedFilters = filters
      this.isFirstLoad = false
      if (isFilterChanged) {
        this.syncFormWithAppliedFilters()
      }
      this.getTools()
    },

    parseRouteQuery(query) {
      const filters = { ...DEFAULT_FILTERS }
      for (const key of Object.keys(DEFAULT_FILTERS)) {
        if (!(key in query)) continue
        const value = Array.isArray(query[key]) ? query[key][0] : query[key]
        const minValue = key === 'pageNumber' ? 1 : 0
        if (!this.isValidInteger(value, minValue)) {
          return { filters, errorMessage: `Vigane URL-i parameeter: ${key}` }
        }
        filters[key] = Number(value)
      }
      return { filters, errorMessage: '' }
    },

    isValidInteger(value, minValue) {
      return (
        typeof value === 'string' &&
        /^\d+$/.test(value) &&
        Number(value) >= minValue &&
        Number(value) <= MAX_INTEGER
      )
    },

    isFilterChanged(filters) {
      return (
        filters.categoryId !== this.appliedFilters.categoryId ||
        filters.cityId !== this.appliedFilters.cityId ||
        filters.districtId !== this.appliedFilters.districtId
      )
    },

    syncFormWithAppliedFilters() {
      this.formFilters = {
        categoryId: this.appliedFilters.categoryId,
        cityId: this.appliedFilters.cityId,
        districtId: this.appliedFilters.districtId,
      }
      if (this.formFilters.cityId !== this.districtsCityId) {
        this.getDistricts(this.formFilters.cityId)
      }
    },

    getTools() {
      const requestId = ++this.toolsRequestId
      this.isToolsLoading = true
      ToolService.sendGetToolsRequest({ ...this.appliedFilters })
        .then((response) => {
          if (requestId === this.toolsRequestId) this.handleGetToolsResponse(response.data)
        })
        .catch((error) => {
          if (requestId === this.toolsRequestId) this.handleGetToolsError(error)
        })
        .finally(() => {
          if (requestId === this.toolsRequestId) this.isToolsLoading = false
        })
    },

    handleGetToolsResponse(toolsResponse) {
      this.toolsResponse = toolsResponse
      this.toolsErrorMessage = ''
      this.isToolsResultStale = false
    },

    handleGetToolsError(error) {
      this.toolsErrorMessage = this.getErrorMessage(error, TOOLS_LOADING_FAILED)
      this.isToolsResultStale = this.toolsResponse !== null
      console.error('Tools request failed', error)
    },

    getCategories() {
      this.isCategoriesLoading = true
      this.categoriesErrorMessage = ''
      CategoryService.sendGetCategoriesRequest()
        .then((response) => this.handleGetCategoriesResponse(response.data))
        .catch((error) => this.handleGetCategoriesError(error))
        .finally(() => {
          this.isCategoriesLoading = false
        })
    },

    handleGetCategoriesResponse(categories) {
      this.categories = categories
      this.isCategoriesLoaded = true
    },

    handleGetCategoriesError(error) {
      this.categoriesErrorMessage = this.getErrorMessage(error, CATEGORIES_LOADING_FAILED)
      console.error('Categories request failed', error)
    },

    getCities() {
      this.isCitiesLoading = true
      this.citiesErrorMessage = ''
      CityService.sendGetCitiesRequest()
        .then((response) => this.handleGetCitiesResponse(response.data))
        .catch((error) => this.handleGetCitiesError(error))
        .finally(() => {
          this.isCitiesLoading = false
        })
    },

    handleGetCitiesResponse(cities) {
      this.cities = cities
    },

    handleGetCitiesError(error) {
      this.citiesErrorMessage = this.getErrorMessage(error, CITIES_LOADING_FAILED)
      console.error('Cities request failed', error)
    },

    getDistricts(cityId) {
      const requestId = ++this.districtsRequestId
      this.districtsCityId = cityId
      this.districts = []
      this.districtsErrorMessage = ''
      if (cityId === 0) {
        this.isDistrictsLoading = false
        return
      }
      this.isDistrictsLoading = true
      CityService.sendGetCityDistrictsRequest(cityId)
        .then((response) => {
          if (requestId === this.districtsRequestId) this.handleGetDistrictsResponse(response.data)
        })
        .catch((error) => {
          if (requestId === this.districtsRequestId) this.handleGetDistrictsError(error)
        })
        .finally(() => {
          if (requestId === this.districtsRequestId) this.isDistrictsLoading = false
        })
    },

    handleGetDistrictsResponse(districts) {
      this.districts = districts
    },

    handleGetDistrictsError(error) {
      this.districts = []
      this.districtsErrorMessage = this.getErrorMessage(error, DISTRICTS_LOADING_FAILED)
      console.error('Districts request failed', error)
    },

    getErrorMessage(error, fallbackMessage) {
      if (!error.response) {
        return NETWORK_ERROR_MESSAGE
      }
      return error.response.data?.message ?? fallbackMessage
    },

    handleCategoryChanged(categoryId) {
      this.formFilters.categoryId = categoryId
    },

    handleCityChanged(cityId) {
      this.formFilters.cityId = cityId
      this.formFilters.districtId = 0
      this.getDistricts(cityId)
    },

    handleDistrictChanged(districtId) {
      this.formFilters.districtId = districtId
    },

    handleApplyFilters() {
      this.navigateWithFilters({ ...this.formFilters, pageNumber: 1 })
    },

    handleResetFilters() {
      this.formFilters = { categoryId: 0, cityId: 0, districtId: 0 }
      if (this.districtsCityId !== 0) {
        this.getDistricts(0)
      }
      this.navigateWithFilters({ ...DEFAULT_FILTERS })
    },

    handlePageChanged(pageNumber) {
      this.navigateWithFilters({ ...this.appliedFilters, pageNumber })
    },

    handleGoToLastPage() {
      this.handlePageChanged(this.toolsResponse.totalPages)
    },

    navigateWithFilters(filters) {
      const query = this.buildQuery(filters)
      if (this.isSameQuery(query, this.$route.query)) {
        this.loadToolsFromRoute()
      } else {
        NavigationService.navigateToToolsSearch(this.$router, query)
      }
    },

    buildQuery(filters) {
      const query = {}
      for (const key of Object.keys(DEFAULT_FILTERS)) {
        if (filters[key] !== DEFAULT_FILTERS[key]) {
          query[key] = String(filters[key])
        }
      }
      return query
    },

    isSameQuery(query, routeQuery) {
      const keys = Object.keys(query)
      const routeKeys = Object.keys(routeQuery)
      return keys.length === routeKeys.length && keys.every((key) => query[key] === routeQuery[key])
    },

    handleViewDetails(toolId) {
      NavigationService.navigateToToolDetail(this.$router, toolId)
    },
  },
  beforeMount() {
    this.getCategories()
    this.getCities()
    this.loadToolsFromRoute()
  },
}
</script>

<style scoped>
.tools {
  max-width: 1200px;
}

.tools-heading {
  max-width: 680px;
}

.tools-eyebrow {
  margin-bottom: var(--space-3);
  color: var(--color-primary);
  font-size: var(--font-size-small);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.tools-heading h1 {
  font-size: clamp(2rem, 4vw, 3rem);
  letter-spacing: -0.04em;
}

.tools-intro {
  margin-top: var(--space-3);
  color: var(--color-muted);
  font-size: var(--font-size-lead);
}

.tools-results-title {
  letter-spacing: -0.025em;
}

.tools-filter-status {
  color: var(--color-muted);
}

.tools-feedback {
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  color: var(--color-muted);
}

.tool-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 991.98px) {
  .tool-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 575.98px) {
  .tool-grid {
    grid-template-columns: 1fr;
  }
}
</style>
