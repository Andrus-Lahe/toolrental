import axios from 'axios'

export default {
  sendGetAdminUsersRequest() {
    return axios.get('/api/admin/users')
  },

  sendPatchUserStatusRequest(userId, status) {
    return axios.patch(`/api/admin/users/${userId}/status`, { status })
  },

  sendDeleteUserRequest(userId) {
    return axios.delete(`/api/admin/users/${userId}`)
  },
}
