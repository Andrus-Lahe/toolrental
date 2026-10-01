<template>
  <select
    class="form-select"
    @change="$emit('event-new-category-selected', Number($event.target.value))"
  >
    <option :value="0" :selected="selectedCategoryId === 0">{{ firstOptionLabel }}</option>
    <option v-if="isSelectedCategoryUnknown" :value="selectedCategoryId" selected disabled>
      Tundmatu kategooria (ID {{ selectedCategoryId }})
    </option>
    <option
      v-for="category in categories"
      :key="category.categoryId"
      :value="category.categoryId"
      :selected="category.categoryId === selectedCategoryId"
    >
      {{ category.categoryName }}
    </option>
  </select>
</template>

<script>
export default {
  name: 'CategoriesDropdown',
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    selectedCategoryId: {
      type: Number,
      default: 0,
    },
    firstOptionLabel: {
      type: String,
      default: 'Kõik kategooriad',
    },
    isLoaded: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-category-selected'],
  computed: {
    isSelectedCategoryUnknown() {
      return (
        this.isLoaded &&
        this.selectedCategoryId > 0 &&
        !this.categories.some((category) => category.categoryId === this.selectedCategoryId)
      )
    },
  },
}
</script>
