<template>
  <div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>
</template>

<script>
export default {
  name: 'AlertDanger',
  props: {
    errorMessage: String,
  },
}
</script>
