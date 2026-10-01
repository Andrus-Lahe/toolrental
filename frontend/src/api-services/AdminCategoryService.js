import axios from 'axios'

export default {
  sendGetAdminCategoriesRequest() {
    return axios.get('/api/admin/categories')
  },

  sendPostCategoryRequest(category) {
    return axios.post('/api/admin/categories', category)
  },

  sendPutCategoryRequest(categoryId, category) {
    return axios.put(`/api/admin/categories/${categoryId}`, category)
  },

  sendDeleteCategoryRequest(categoryId) {
    return axios.delete(`/api/admin/categories/${categoryId}`)
  },
}
