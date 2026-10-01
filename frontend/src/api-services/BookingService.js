import axios from 'axios'

export default {
  sendGetBookingRequest(bookingId) {
    return axios.get(`/api/bookings/${bookingId}`)
  },

  sendConfirmBookingRequest(bookingId, bookingDecisionRequest) {
    return axios.patch(`/api/bookings/${bookingId}/confirm`, bookingDecisionRequest)
  },

  sendRejectBookingRequest(bookingId, bookingDecisionRequest) {
    return axios.patch(`/api/bookings/${bookingId}/reject`, bookingDecisionRequest)
  },

  sendCreateBookingRequest(booking) {
    return axios.post('/api/bookings', booking)
  },
}
