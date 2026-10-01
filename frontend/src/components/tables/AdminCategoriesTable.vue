<template>
  <div class="table-responsive categories-table">
    <table class="table table-striped align-middle">
      <thead>
        <tr>
          <th scope="col">Kategooria nimi</th>
          <th scope="col">Tegevus</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="category in categories" :key="category.categoryId">
          <td>{{ category.categoryName }}</td>
          <td>
            <div class="d-flex gap-2">
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                :disabled="pendingCategoryId === category.categoryId"
                @click="$emit('event-edit-category', category)"
              >
                Muuda
              </button>
              <button
                type="button"
                class="btn btn-outline-danger btn-sm"
                :disabled="pendingCategoryId === category.categoryId"
                @click="$emit('event-delete-category', category)"
              >
                Kustuta
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'AdminCategoriesTable',
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    pendingCategoryId: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-edit-category', 'event-delete-category'],
}
</script>

<style scoped>
.categories-table {
  max-width: 520px;
}
</style>
