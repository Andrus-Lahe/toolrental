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

  navigateToBookingFormView(router, toolId) {
    return router.push({ name: 'bookingFormRoute', params: { toolId } })
  },

  navigateToBookingDetails(router, bookingId) {
    return router.push({ path: `/bookings/${bookingId}` })
  },

  navigateToBookingApproval(router, bookingId) {
    return router.push({ path: `/bookings/${bookingId}` })
  },

  navigateBackOrToToolDetail(router, toolId) {
    if (window.history.state?.back) return router.back()
    return this.navigateToToolDetail(router, toolId)
  },

  navigateToToolDetail(router, toolId) {
    return router.push({ path: `/tools/${toolId}` })
  },

  navigateToHomeView(router) {
    return router.push({ name: 'homeRoute' })
  },

  navigateToToolsSearch(router, query) {
    return router.push({ path: '/tools', query })
  },
}
