export default {
  navigateToTools(router, categoryId) {
    return router.push({ path: '/tools', query: { categoryId: String(categoryId) } })
  },

  navigateToAddTool(router) {
    return router.push({ path: '/tools/new' })
  },

  navigateToMyTools(router, successMessage = '') {
    return router.push({ path: '/my-tools', query: successMessage ? { successMessage } : {} })
  },

  navigateToProfile(router) {
    return router.push({ path: '/profile' })
  },
}
