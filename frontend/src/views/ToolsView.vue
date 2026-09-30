<template>
  <main class="container py-5">
    <h1>Otsing</h1>
    <p v-if="categoryId">Valitud kategooria järgi otsimine on koostamisel.</p>
    <p v-else>Tööriistade otsinguvaade on koostamisel.</p>
  </main>
</template>

<script>
export default {
  name: 'ToolsView',
  computed: {
    categoryId() {
      return this.$route.query.categoryId ?? ''
    },
  },
}
</script>
