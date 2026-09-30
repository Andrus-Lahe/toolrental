<template>
  <main class="container py-4">
    <h1>Otsi tööriistu küsimusega</h1>
    <p>Kirjuta näiteks: „Millised tööriistad on saadaval Kristiines?”</p>

    <form class="mb-4" @submit.prevent="ask">
      <label class="form-label" for="tool-question">Sinu küsimus</label>
      <textarea
        id="tool-question"
        v-model.trim="question"
        class="form-control mb-2"
        rows="3"
        maxlength="500"
        required
      />
      <button class="btn btn-primary" type="submit" :disabled="busy || !question">
        {{ busy ? 'Otsin…' : 'Küsi' }}
      </button>
    </form>

    <p v-if="error" class="alert alert-danger" role="alert">{{ error }}</p>
    <p v-if="needsLogin"><a href="/oauth2/authorization/google">Logi Google’iga sisse</a></p>

    <section v-if="answer" aria-live="polite">
      <h2>Vastus</h2>
      <p>{{ answer }}</p>
      <ul v-if="tools.length" class="list-group">
        <li v-for="tool in tools" :key="tool.toolId" class="list-group-item">
          <strong>{{ tool.name }}</strong> · {{ tool.category }}
          <span v-if="tool.district"> · {{ tool.district }}</span>
        </li>
      </ul>
    </section>
  </main>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AiSearchView',
  data() {
    return {
      question: '',
      answer: '',
      tools: [],
      busy: false,
      error: '',
      needsLogin: false,
    }
  },
  methods: {
    ask() {
      if (!this.question || this.busy) return
      this.busy = true
      this.error = ''
      this.answer = ''
      this.tools = []
      this.needsLogin = false

      axios.post('/api/ai/ask', { question: this.question })
        .then((response) => {
          this.answer = response.data.answer
          this.tools = response.data.tools ?? []
        })
        .catch((error) => {
          if (error.response?.status === 401) {
            this.needsLogin = true
            this.error = 'Tööriistade küsimiseks logi esmalt sisse.'
          } else {
            this.error = 'Päring ebaõnnestus. Palun proovi uuesti.'
          }
        })
        .finally(() => {
          this.busy = false
        })
    },
  },
}
</script>
