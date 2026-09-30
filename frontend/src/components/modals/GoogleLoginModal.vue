<template>
  <div v-if="isOpen" class="modal-backdrop-custom" @click.self="$emit('event-modal-closed')">
    <section class="login-dialog" role="dialog" aria-modal="true" aria-labelledby="login-title">
      <button
        type="button"
        class="btn-close float-end"
        aria-label="Sulge"
        @click="$emit('event-modal-closed')"
      ></button>
      <h2 id="login-title" class="h4">Logi sisse</h2>
      <p>Meie kodulehele saad sisse logida Google kontoga</p>
      <a class="btn btn-primary" href="/oauth2/authorization/google">Jätka Google kontoga</a>
    </section>
  </div>
</template>

<script>
export default {
  name: 'GoogleLoginModal',
  props: { isOpen: Boolean },
  emits: ['event-modal-closed'],
}
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  z-index: 1050;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgb(0 0 0 / 50%);
}
.login-dialog {
  width: min(100%, 430px);
  padding: 1.5rem;
  border-radius: 0.5rem;
  background: white;
}
</style>
