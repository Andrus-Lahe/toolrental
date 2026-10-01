<template>
  <form class="tools-filter-form" @submit.prevent="$emit('event-apply-filters')">
    <h2 class="h5 mb-4">Filtreeri</h2>

    <div class="mb-3">
      <label for="tools-filter-category" class="form-label">Kategooria</label>
      <CategoriesDropdown
        id="tools-filter-category"
        :categories="categories"
        :selected-category-id="selectedCategoryId"
        :is-loaded="isCategoriesLoaded"
        first-option-label="Vali kategooria"
        @event-new-category-selected="(categoryId) => $emit('event-category-changed', categoryId)"
      />
    </div>

    <div class="mb-3">
      <label for="tools-filter-city" class="form-label">Linn</label>
      <CitiesDropdown
        id="tools-filter-city"
        :cities="cities"
        :selected-city-id="selectedCityId"
        first-option-label="Vali linn"
        @event-new-city-selected="(cityId) => $emit('event-city-changed', cityId)"
      />
    </div>

    <div class="mb-3">
      <label for="tools-filter-district" class="form-label">Linnaosa</label>
      <DistrictsDropdown
        id="tools-filter-district"
        :districts="districts"
        :selected-district-id="selectedDistrictId"
        :is-disabled="isDistrictsDisabled"
        first-option-label="Vali linnaosa"
        @event-new-district-selected="(districtId) => $emit('event-district-changed', districtId)"
      />
      <p v-if="isDistrictsLoading" class="form-text mb-0" role="status">Linnaosade laadimine…</p>
    </div>

    <div class="d-grid gap-2">
      <button type="submit" class="btn btn-primary">Rakenda</button>
      <button type="button" class="btn btn-outline-secondary" @click="$emit('event-reset-filters')">
        Tühista filtrid
      </button>
    </div>
  </form>
</template>

<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import CitiesDropdown from '@/components/forms/CitiesDropdown.vue'
import DistrictsDropdown from '@/components/forms/DistrictsDropdown.vue'

export default {
  name: 'ToolsFilterForm',
  components: { CategoriesDropdown, CitiesDropdown, DistrictsDropdown },
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    cities: {
      type: Array,
      default: () => [],
    },
    districts: {
      type: Array,
      default: () => [],
    },
    selectedCategoryId: {
      type: Number,
      default: 0,
    },
    selectedCityId: {
      type: Number,
      default: 0,
    },
    selectedDistrictId: {
      type: Number,
      default: 0,
    },
    isCategoriesLoaded: {
      type: Boolean,
      default: false,
    },
    isDistrictsLoading: {
      type: Boolean,
      default: false,
    },
    isDistrictsUnavailable: {
      type: Boolean,
      default: false,
    },
  },
  emits: [
    'event-category-changed',
    'event-city-changed',
    'event-district-changed',
    'event-apply-filters',
    'event-reset-filters',
  ],
  computed: {
    isDistrictsDisabled() {
      return this.selectedCityId === 0 || this.isDistrictsLoading || this.isDistrictsUnavailable
    },
  },
}
</script>

<style scoped>
.tools-filter-form {
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.tools-filter-form h2 {
  color: var(--color-ink);
  letter-spacing: -0.025em;
}

.tools-filter-form .form-label {
  margin-bottom: var(--space-2);
}

.tools-filter-form .d-grid {
  margin-top: var(--space-5);
}

@media (max-width: 575.98px) {
  .tools-filter-form {
    padding: var(--space-4);
  }
}
</style>
