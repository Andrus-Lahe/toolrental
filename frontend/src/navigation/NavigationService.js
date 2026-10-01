export default {
  navigateToTools(router, categoryId) {
    return router.push({ path: '/tools', query: { categoryId: String(categoryId) } })
  },

  navigateToHomeView(router) {
    return router.push({ name: 'homeRoute' })
  },
}
