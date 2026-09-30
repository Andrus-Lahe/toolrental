import axios from 'axios'

export default {
  sendGetCurrentUserRequest() {
    return axios.get('/api/me')
  },
}
