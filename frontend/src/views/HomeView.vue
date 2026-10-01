<template>
  <main class="home container py-5">
    <section class="mb-5" aria-labelledby="home-title">
      <h1 id="home-title">Laena tööriistu naabritelt</h1>
      <p class="home-intro mt-3">
        Meie kogukonna kõige parem kraami jagamise pleiss. Tööriistade sirvimiseks ja
        lisamiseks pead olema sisse logitud
      </p>
    </section>

    <section aria-labelledby="categories-title" class="mb-5">
      <h2 id="categories-title" class="mb-4">Saadaolevad tööriistad</h2>
      <p v-if="session.status === 'loading'" role="status">Sisselogimise oleku laadimine...</p>
      <p v-if="isLoading" role="status">Kategooriate laadimine...</p>
      <div v-else-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>
      <p v-else-if="categories.length === 0">Kategooriaid ei leitud.</p>
      <div v-else class="category-grid">
        <CategoryCard
          v-for="category in categories"
          :key="category.categoryId"
          :category="category"
          @event-category-selected="handleCategorySelected"
        />
      </div>
    </section>

    <section aria-labelledby="how-it-works-title">
      <h2 id="how-it-works-title" class="mb-4">Kuidas see töötab</h2>
      <div class="info-grid" aria-hidden="true">
        <div v-for="index in 3" :key="index" class="info-placeholder"></div>
      </div>
    </section>
  </main>
</template>

<script>
import CategoryCard from '@/components/common/CategoryCard.vue'
import CategoryService from '@/api-services/CategoryService.js'
import NavigationService from '@/navigation/NavigationService.js'
import { session } from '@/auth/session.js'

export default {
  name: 'HomeView',
  components: { CategoryCard },
  inject: ['openLoginModal'],
  data() {
    return {
      categories: [],
      isLoading: true,
      errorMessage: '',
      session,
    }
  },
  methods: {
    getCategories() {
      CategoryService.sendGetCategoriesDetailedInfoRequest()
        .then((response) => this.handleCategoriesResponse(response.data))
        .catch((error) => this.handleCategoriesError(error))
        .finally(() => {
          this.isLoading = false
        })
    },
    handleCategoriesResponse(categories) {
      this.categories = categories
    },
    handleCategoriesError(error) {
      this.errorMessage = 'Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.'
      console.error('Category request failed', error)
    },
    handleCategorySelected(categoryId) {
      if (this.session.status === 'authenticated') {
        NavigationService.navigateToTools(this.$router, categoryId)
      } else if (this.session.status === 'guest') {
        this.openLoginModal()
      }
    },
  },
  beforeMount() {
    this.getCategories()
  },
}
</script>

<style scoped>
.home { max-width: 1100px; }
.home-intro { max-width: 650px; }
.category-grid,
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.25rem;
}
.category-grid > :last-child:nth-child(3n + 1) { grid-column: 2; }
.info-placeholder { min-height: 150px; border: 1px solid #adb5bd; }
@media (max-width: 767px) {
  .category-grid, .info-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .category-grid > :last-child:nth-child(3n + 1) { grid-column: auto; }
}
@media (max-width: 575px) {
  .category-grid, .info-grid { grid-template-columns: 1fr; }
}
</style>
