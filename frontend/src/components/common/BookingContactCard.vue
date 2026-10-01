<template>
  <section class="card h-100" aria-labelledby="booking-contact-title">
    <div class="card-body">
      <h2 id="booking-contact-title" class="h5">Kontaktandmed</h2>
      <p class="fw-semibold mb-3">{{ displayName }}</p>
      <p class="mb-2">
        <a
          v-if="emailAddress"
          :href="emailComposeUrl"
          target="_blank"
          rel="noopener noreferrer"
        >{{ emailAddress }}</a>
        <span v-else>—</span>
      </p>
      <p class="mb-0">{{ contactPhone || '—' }}</p>
    </div>
  </section>
</template>

<script>
export default {
  name: 'BookingContactCard',
  props: {
    contactName: { type: String, default: '' },
    contactEmail: { type: String, default: null },
    contactPhone: { type: String, default: null },
  },
  computed: {
    displayName() {
      return this.contactName?.trim() || '—'
    },
    emailComposeUrl() {
      return `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(this.emailAddress)}`
    },
    emailAddress() {
      return this.contactEmail?.trim() || ''
    },
  },
}
</script>
