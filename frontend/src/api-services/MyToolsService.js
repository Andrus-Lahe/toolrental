import axios from 'axios'

export default {
  sendGetMyToolsRequest() {
    return axios.get('/api/users/me/tools')
  },
}
