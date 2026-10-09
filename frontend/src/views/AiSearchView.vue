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

    <section v-if="hasSearched" aria-live="polite">
      <ul v-if="tools.length" class="list-group">
        <li v-for="tool in tools" :key="tool.toolId" class="list-group-item">
          <RouterLink :to="{ name: 'toolDetailRoute', params: { toolId: tool.toolId } }">
            {{ tool.name }}
          </RouterLink>
          · {{ tool.category }}<span v-if="tool.location"> · {{ tool.location }}</span>
        </li>
      </ul>
      <p v-else role="status">Sellele otsingule vastavaid praegu saadaval tööriistu ei leitud.</p>
    </section>
  </main>
</template>

<script>
import axios from 'axios'
import { RouterLink } from 'vue-router'

function isValidToolSearchResponse(data) {
  if (!data || !Array.isArray(data.tools)) return false

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
      tools: [],
      hasSearched: false,
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
      this.tools = []
      this.hasSearched = false
      this.needsLogin = false

      return axios.post('/api/ai/ask', { question: this.question })
        .then((response) => {
          if (!isValidToolSearchResponse(response.data)) {
            throw new Error('Invalid AI tool search response')
          }
          this.tools = response.data.tools
          this.hasSearched = true
        })
        .catch((error) => {
          this.tools = []
          this.hasSearched = false
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
