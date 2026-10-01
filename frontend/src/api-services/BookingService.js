import axios from 'axios'

export default {
  sendCreateBookingRequest(booking) {
    return axios.post('/api/bookings', booking)
  },
}
