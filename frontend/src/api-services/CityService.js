import axios from 'axios'

export default {
  sendGetCitiesRequest() {
    return axios.get('/api/cities')
  },

  sendGetCityDistrictsRequest(cityId) {
    return axios.get(`/api/cities/${cityId}/districts`)
  },
}
