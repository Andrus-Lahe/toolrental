<template>
  <section class="card" aria-labelledby="profile-card-title">
    <div class="card-body">
      <h2 id="profile-card-title" class="h5">Kontaktandmed</h2>
      <p class="fw-semibold mb-2">{{ fullName }}</p>
      <p class="mb-2 text-break">{{ profile.email || 'E-post puudub' }}</p>
      <p class="mb-3">{{ profile.phone || 'Telefon puudub' }}</p>
      <button
        type="button"
        class="btn btn-outline-primary btn-sm"
        :disabled="isActionDisabled"
        @click="$emit('event-edit-profile')"
      >
        Muuda profiili
      </button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'UserProfileCard',
  props: {
    profile: { type: Object, required: true },
    isActionDisabled: { type: Boolean, default: false },
  },
  emits: ['event-edit-profile'],
  computed: {
    fullName() {
      return [this.profile.firstName, this.profile.lastName]
        .map((part) => (part ?? '').trim())
        .filter((part) => part !== '')
        .join(' ') || 'Kasutaja'
    },
  },
}
</script>
