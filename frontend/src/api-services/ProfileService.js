import axios from 'axios'

export default {
  sendGetMyProfileRequest() {
    return axios.get('/api/users/me/profile')
  },

  sendPutMyProfileRequest(profile) {
    return axios.put('/api/users/me/profile', profile)
  },
}
