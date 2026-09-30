import axios from 'axios'

export default {
  sendGetCategoriesDetailedInfoRequest() {
    return axios.get('/api/categories/detailed-info')
  },
}
