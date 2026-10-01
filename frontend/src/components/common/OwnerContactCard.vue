<template>
  <section class="owner-contact-card border rounded p-3" aria-labelledby="owner-contact-heading">
    <h2 id="owner-contact-heading" class="h5">Omaniku kontaktinfo</h2>
    <p class="mb-2">{{ owner.firstName }} {{ owner.lastName }}</p>
    <p class="mb-2">
      <a :href="`mailto:${owner.email}`">{{ owner.email }}</a>
    </p>
    <p class="mb-0">
      <a :href="`tel:${owner.phone}`">{{ owner.phone }}</a>
    </p>
  </section>
</template>

<script>
export default {
  name: 'OwnerContactCard',
  props: {
    owner: { type: Object, required: true },
  },
}
</script>
