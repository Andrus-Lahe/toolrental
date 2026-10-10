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
        @keydown.enter="handleQuestionKeydown"
      />
      <button class="btn btn-primary" type="submit" :disabled="busy || !question">
        {{ busy ? 'Otsin…' : 'Küsi' }}
      </button>
    </form>

    <p v-if="error" class="alert alert-danger" role="alert">{{ error }}</p>
    <p v-if="needsLogin"><a href="/oauth2/authorization/google">Logi Google’iga sisse</a></p>

    <section v-if="hasSearched" aria-live="polite">
      <p>{{ answer }}</p>
      <ul v-if="tools.length" class="list-group">
        <li v-for="tool in tools" :key="tool.toolId" class="list-group-item">
          <RouterLink :to="{ name: 'toolDetailRoute', params: { toolId: tool.toolId } }">
            {{ tool.name }}
          </RouterLink>
          · {{ tool.category }}<span v-if="tool.location"> · {{ tool.location }}</span>
        </li>
      </ul>
    </section>
  </main>
</template>

<script>
import axios from 'axios'
import { RouterLink } from 'vue-router'

function isValidToolSearchResponse(data) {
  if (!data || typeof data.answer !== 'string' || !data.answer.trim() || !Array.isArray(data.tools)) {
    return false
  }

  const ids = new Set()
  return data.tools.every((tool) => {
    if (!tool || !Number.isInteger(tool.toolId) || tool.toolId < 1 || tool.toolId > 2147483647) return false
    if (ids.has(tool.toolId)) return false
    if (typeof tool.name !== 'string' || !tool.name.trim()) return false
    if (typeof tool.category !== 'string' || !tool.category.trim()) return false
    if (tool.location !== null && (typeof tool.location !== 'string' || !tool.location.trim())) return false

    ids.add(tool.toolId)
    return true
  })
}

export default {
  name: 'AiSearchView',
  components: { RouterLink },
  data() {
    return {
      question: '',
      answer: '',
      tools: [],
      hasSearched: false,
      busy: false,
      error: '',
      needsLogin: false,
    }
  },
  methods: {
    handleQuestionKeydown(event) {
      if (event.shiftKey) return
      event.preventDefault()
      this.ask()
    },
    ask() {
      if (!this.question || this.busy) return
      this.busy = true
      this.error = ''
      this.answer = ''
      this.tools = []
      this.hasSearched = false
      this.needsLogin = false

      return axios.post('/api/ai/ask', { question: this.question })
        .then((response) => {
          if (!isValidToolSearchResponse(response.data)) {
            throw new Error('Invalid AI tool search response')
          }
          this.answer = response.data.answer
          this.tools = response.data.tools
          this.hasSearched = true
        })
        .catch((error) => {
          this.answer = ''
          this.tools = []
          this.hasSearched = false
          if (error.response?.status === 401) {
            this.needsLogin = true
            this.error = 'Tööriistade küsimiseks logi esmalt sisse.'
          } else if (error.response?.status === 400
            && error.response.data?.errorCode === 'INCORRECT_INPUT') {
            this.error = 'Palun sisesta küsimus, mis ei ole tühi ja on kuni 500 märki pikk.'
          } else if (error.response?.status === 422
            && error.response.data?.errorCode === 'AI_UNSUPPORTED_QUESTION') {
            this.error = 'Saan aidata vaid tööriistade otsinguda.'
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
