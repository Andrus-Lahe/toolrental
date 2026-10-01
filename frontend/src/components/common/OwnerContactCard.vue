<template>
  <section class="owner-contact-card border rounded p-3" aria-labelledby="owner-contact-heading">
    <h2 id="owner-contact-heading" class="h5">Omaniku kontaktinfo</h2>
    <p class="mb-2">{{ owner.firstName }} {{ owner.lastName }}</p>
    <p v-if="owner.email" class="mb-2">
      <PhEnvelope class="me-2" aria-hidden="true" />
      <a :href="`mailto:${owner.email}`">{{ owner.email }}</a>
    </p>
    <p v-if="owner.phone" class="mb-0">
      <PhDeviceMobile class="me-2" aria-hidden="true" />
      <a :href="`tel:${owner.phone}`">{{ owner.phone }}</a>
    </p>
  </section>
</template>

<script>
import { PhDeviceMobile, PhEnvelope } from '@phosphor-icons/vue'

export default {
  name: 'OwnerContactCard',
  components: { PhEnvelope, PhDeviceMobile },
  props: {
    owner: { type: Object, required: true },
  },
}
</script>
