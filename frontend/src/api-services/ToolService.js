import axios from 'axios'

export default {
  sendGetToolDetailsRequest(toolId) {
    return axios.get(`/api/tools/${toolId}`)
  },

  sendCreateToolRequest(tool) {
    return axios.post('/api/tools', tool)
  },
}
