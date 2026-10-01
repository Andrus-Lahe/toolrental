<template>
  <nav aria-label="Lehekülgede valik">
    <ul class="pagination flex-wrap justify-content-center mb-0">
      <li class="page-item" :class="{ disabled: isPreviousDisabled }">
        <button
          type="button"
          class="page-link"
          :disabled="isPreviousDisabled"
          @click="selectPage(pageNumber - 1)"
        >
          Eelmine
        </button>
      </li>

      <li
        v-for="item in pageItems"
        :key="item.key"
        class="page-item"
        :class="{ active: item.page === pageNumber, disabled: item.page === null }"
      >
        <span v-if="item.page === null" class="page-link">…</span>
        <button
          v-else
          type="button"
          class="page-link"
          :aria-current="item.page === pageNumber ? 'page' : null"
          :disabled="isDisabled"
          @click="selectPage(item.page)"
        >
          {{ item.page }}
        </button>
      </li>

      <li class="page-item" :class="{ disabled: isNextDisabled }">
        <button
          type="button"
          class="page-link"
          :disabled="isNextDisabled"
          @click="selectPage(pageNumber + 1)"
        >
          Järgmine
        </button>
      </li>
    </ul>
  </nav>
</template>

<script>
const MAX_VISIBLE_PAGES = 7

export default {
  name: 'ToolsPagination',
  props: {
    pageNumber: {
      type: Number,
      default: 1,
    },
    totalPages: {
      type: Number,
      default: 0,
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-page-changed'],
  computed: {
    isPreviousDisabled() {
      return this.isDisabled || this.totalPages === 0 || this.pageNumber <= 1
    },
    isNextDisabled() {
      return this.isDisabled || this.totalPages === 0 || this.pageNumber >= this.totalPages
    },
    pageItems() {
      const pages = this.getVisiblePages()
      const items = []
      pages.forEach((page, index) => {
        if (index > 0 && page - pages[index - 1] > 1) {
          items.push({ key: `gap-${page}`, page: null })
        }
        items.push({ key: `page-${page}`, page })
      })
      return items
    },
  },
  methods: {
    getVisiblePages() {
      const total = this.totalPages
      if (total <= MAX_VISIBLE_PAGES) {
        return Array.from({ length: total }, (_, index) => index + 1)
      }
      const current = Math.min(Math.max(this.pageNumber, 1), total)
      const pages = new Set([1, total, current - 1, current, current + 1])
      return [...pages].filter((page) => page >= 1 && page <= total).sort((a, b) => a - b)
    },
    selectPage(page) {
      if (page !== this.pageNumber && page >= 1) {
        this.$emit('event-page-changed', page)
      }
    },
  },
}
</script>
