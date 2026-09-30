import router from '@/router'

export default {
  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },

  navigateToProfileView() {
    router.push({ name: 'profileRoute' })
  },
}
