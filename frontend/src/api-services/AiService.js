import axios from 'axios'

export default {
  sendAskRequest(question) {
    return axios.post('/api/ai/ask', { question })
  },
}
