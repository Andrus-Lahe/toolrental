<template>
  <main class="container py-5">
    <div v-if="successMessage" class="alert alert-success" role="status">
      {{ successMessage }}
    </div>
    <h1 class="h2">Minu tööriistad rendiks</h1>
  </main>
</template>

<script>
export default {
  name: 'MyToolsView',
  computed: {
    successMessage() {
      return this.$route.query.successMessage ?? ''
    },
  },
}
</script>
