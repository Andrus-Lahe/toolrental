<template>
  <AppHeader />

  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
import AppHeader from '@/navigation/AppHeader.vue'
</script>
