import axios from 'axios'

export default {
  sendCreateToolRequest(tool) {
    return axios.post('/api/tools', tool)
  },
}
