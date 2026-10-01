import axios from 'axios'

export default {
  sendGetCategoriesRequest() {
    return axios.get('/api/categories')
  },

  sendGetCategoriesDetailedInfoRequest() {
    return axios.get('/api/categories/detailed-info')
  },
}
