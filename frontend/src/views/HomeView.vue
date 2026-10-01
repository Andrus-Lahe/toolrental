<template>
  <main class="home container py-4 py-lg-5">
    <section class="home-hero mb-5" aria-labelledby="home-title">
      <div class="home-hero-copy">
        <p class="home-eyebrow">Laenukas · tööriistad sinu naabruskonnast</p>
        <h1 id="home-title">Laena tööriistu naabritelt</h1>
        <p class="home-intro">
          Meie kogukonna koht tööriistade jagamiseks. Leia vajalik tööriist lähedalt või jaga
          enda oma naabritega.
        </p>
        <div class="home-hero-actions">
          <RouterLink class="btn btn-primary btn-lg" to="/tools">Otsi tööriistu</RouterLink>
          <a class="home-secondary-link" href="#how-it-works-title">Kuidas see käib?</a>
        </div>
      </div>
      <div class="home-hero-note" aria-hidden="true">
        <span class="home-hero-note-mark">L</span>
        <p class="mb-1">Kasuta seda, mida juba leidub.</p>
        <span>Jaga oskusi ja tööriistu oma kogukonnas.</span>
      </div>
    </section>

    <section aria-labelledby="categories-title" class="mb-5">
      <div class="section-heading mb-4">
        <div>
          <p class="section-eyebrow">Leia sobiv abiline</p>
          <h2 id="categories-title" class="mb-0">Sirvi kategooriaid</h2>
        </div>
        <RouterLink class="home-all-tools-link" to="/tools">Kõik tööriistad <span aria-hidden="true">→</span></RouterLink>
      </div>
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
      <div class="section-heading mb-4">
        <div>
          <p class="section-eyebrow">Lihtne algusest lõpuni</p>
          <h2 id="how-it-works-title" class="mb-0">Kuidas see käib?</h2>
        </div>
      </div>
      <div class="info-grid">
        <article class="info-card">
          <span class="info-card-number">01</span>
          <h3 class="h5">Leia vajalik</h3>
          <p class="mb-0">Sirvi kategooriaid või otsi tööriista oma piirkonnast.</p>
        </article>
        <article class="info-card">
          <span class="info-card-number">02</span>
          <h3 class="h5">Leppige kokku</h3>
          <p class="mb-0">Saada laenusoov ja lepi omanikuga aeg kokku.</p>
        </article>
        <article class="info-card">
          <span class="info-card-number">03</span>
          <h3 class="h5">Jaga edasi</h3>
          <p class="mb-0">Kui oled töö lõpetanud, saab sama tööriista kasutada järgmine naaber.</p>
        </article>
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
.home {
  max-width: 1200px;
  color: var(--color-ink);
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(260px, 0.65fr);
  align-items: center;
  gap: clamp(2rem, 6vw, 5rem);
  min-height: 340px;
  padding: clamp(1.5rem, 5vw, 4rem);
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-panel);
  background: var(--color-surface-soft);
}

.home-hero-copy {
  min-width: 0;
  max-width: 680px;
}

.home-eyebrow,
.section-eyebrow {
  margin-bottom: var(--space-3);
  color: var(--color-primary);
  font-size: var(--font-size-small);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.home-eyebrow,
.home-intro {
  overflow-wrap: anywhere;
}

.home h1 {
  max-width: 12ch;
  margin: 0;
  font-size: var(--font-size-title);
  letter-spacing: -0.045em;
}

.home-intro {
  max-width: 610px;
  margin: var(--space-4) 0 0;
  color: var(--color-muted);
  font-size: var(--font-size-lead);
}

.home-hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-5);
}

.home-secondary-link,
.home-all-tools-link {
  font-weight: 650;
}

.home-hero-note {
  box-sizing: border-box;
  max-width: 300px;
  justify-self: center;
  padding: var(--space-5);
  border: 1px solid rgb(82 122 99 / 18%);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
  color: var(--color-muted);
}

.home-hero-note p {
  color: var(--color-ink);
  font-size: 1.15rem;
  font-weight: 650;
}

.home-hero-note-mark {
  display: grid;
  width: 2.75rem;
  aspect-ratio: 1;
  margin-bottom: var(--space-4);
  place-items: center;
  border-radius: 0.9rem;
  background: var(--color-primary);
  color: white;
  font-size: 1.25rem;
  font-weight: 700;
}

.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: var(--space-4);
}

.section-heading h2 {
  letter-spacing: -0.025em;
}

.category-grid,
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

.category-grid > :last-child:nth-child(3n + 1) {
  grid-column: 2;
}

.info-card {
  min-height: 170px;
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  background: var(--color-surface);
}

.info-card-number {
  display: inline-block;
  margin-bottom: var(--space-4);
  color: var(--color-primary);
  font-size: var(--font-size-small);
  font-weight: 700;
  letter-spacing: 0.08em;
}

.info-card h3 {
  margin-bottom: var(--space-2);
}

.info-card p {
  color: var(--color-muted);
}

@media (max-width: 767px) {
  .home-hero {
    display: flex;
    width: 100%;
    max-width: 100%;
    flex-direction: column;
    gap: var(--space-5);
  }

  .home-hero-copy,
  .home-hero-note {
    width: 100%;
    max-width: 100%;
  }

  .home-hero-note {
    width: 100%;
    min-width: 0;
    max-width: none;
    justify-self: stretch;
  }

  .category-grid,
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .category-grid > :last-child:nth-child(3n + 1) {
    grid-column: auto;
  }
}

@media (max-width: 575px) {
  .home-hero {
    min-height: 0;
    padding: 1.5rem;
  }

  .home-hero-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .home-hero-actions .btn {
    width: 100%;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .category-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
