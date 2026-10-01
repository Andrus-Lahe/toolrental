import axios from 'axios'

export default {
  sendGetUserDetailsRequest(userId) {
    return axios.get(`/api/users/${userId}`)
  },
}
