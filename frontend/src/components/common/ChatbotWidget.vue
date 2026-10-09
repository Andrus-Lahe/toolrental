<template>
  <div v-if="isLoggedIn" class="chatbot" @keydown.esc="closeChat">
    <button
      v-if="!isOpen"
      ref="toggleButton"
      type="button"
      class="btn btn-primary chatbot-toggle"
      aria-label="Ava vestlus Abimees Petsiga"
      @click="openChat"
    >
      <PhChatCircleDots :size="28" aria-hidden="true" />
    </button>

    <section v-else class="chatbot-window" role="dialog" aria-label="Abimees Pets">
      <header class="chatbot-header">
        <PhChatsCircle :size="22" aria-hidden="true" />
        <h2 class="chatbot-title">Abimees Pets</h2>
        <button type="button" class="chatbot-close" aria-label="Sulge vestlus" @click="closeChat">
          <PhX :size="20" aria-hidden="true" />
        </button>
      </header>

      <div ref="messageList" class="chatbot-messages" aria-live="polite" @scroll="handleScroll">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="chatbot-message"
          :class="{
            'chatbot-message-user': message.sender === 'user',
            'chatbot-message-error': message.isError,
          }"
        >
          <p class="mb-0">{{ message.text }}</p>
          <ul v-if="message.tools.length" class="chatbot-tools">
            <li v-for="tool in message.tools" :key="tool.toolId">
              <a href="#" @click.prevent="openTool(tool.toolId)">
                <strong>{{ tool.name }}</strong> · {{ tool.category }}
                <span v-if="tool.district"> · {{ tool.district }}</span>
              </a>
            </li>
          </ul>
        </div>
        <div v-if="isSending" class="chatbot-message chatbot-message-pending">Pets mõtleb…</div>
      </div>

      <button
        v-if="!isAtBottom"
        type="button"
        class="chatbot-scroll-down"
        aria-label="Keri viimase sõnumini"
        @click="scrollToBottom"
      >
        <PhArrowDown :size="22" aria-hidden="true" />
      </button>

      <form class="chatbot-input" @submit.prevent="sendQuestion">
        <textarea
          ref="questionInput"
          v-model.trim="question"
          class="form-control"
          rows="1"
          maxlength="500"
          placeholder="Kirjuta siia..."
          aria-label="Sinu küsimus"
          @keydown.enter.exact.prevent="sendQuestion"
        />
        <button type="submit" class="btn btn-primary" aria-label="Saada" :disabled="!canSend">
          <PhPaperPlaneRight :size="22" aria-hidden="true" />
        </button>
      </form>
    </section>
  </div>
</template>

<script>
import {
  PhArrowDown,
  PhChatCircleDots,
  PhChatsCircle,
  PhPaperPlaneRight,
  PhX,
} from '@phosphor-icons/vue'
import AiService from '@/api-services/AiService.js'
import NavigationService from '@/navigation/NavigationService.js'
import { loadSession, session } from '@/auth/session.js'

const GREETING = 'Tere! Mina olen Abimees Pets. Kuidas saan sind täna aidata?'
const REQUEST_FAILED = 'Päring ebaõnnestus. Palun proovi uuesti.'

export default {
  name: 'ChatbotWidget',
  components: { PhArrowDown, PhChatCircleDots, PhChatsCircle, PhPaperPlaneRight, PhX },
  data() {
    return {
      session,
      isOpen: false,
      isSending: false,
      isAtBottom: true,
      question: '',
      // { sender: 'bot' | 'user', text: '', tools: [], isError: false }
      messages: [],
    }
  },
  computed: {
    isLoggedIn() {
      return this.session.status === 'authenticated'
    },
    canSend() {
      return this.question !== '' && !this.isSending
    },
  },
  watch: {
    // Väljalogimisel või aegunud sessiooni korral suletakse aken ja vestlus kustutatakse.
    isLoggedIn(loggedIn) {
      if (!loggedIn) {
        this.isOpen = false
        this.messages = []
        this.question = ''
      }
    },
  },
  methods: {
    openChat() {
      if (this.messages.length === 0) {
        this.addMessage('bot', GREETING)
      }
      this.isOpen = true
      this.$nextTick(() => {
        this.scrollToBottom()
        this.$refs.questionInput.focus()
      })
    },

    closeChat() {
      if (!this.isOpen) return
      this.isOpen = false
      this.$nextTick(() => this.$refs.toggleButton?.focus())
    },

    sendQuestion() {
      if (!this.canSend) return
      const question = this.question
      this.addMessage('user', question)
      this.question = ''
      this.isSending = true
      this.scrollToBottomAfterRender()

      AiService.sendAskRequest(question)
        .then((response) => this.handleAskResponse(response.data))
        .catch((error) => this.handleAskError(error))
        .finally(() => {
          this.isSending = false
          this.scrollToBottomAfterRender()
        })
    },

    handleAskResponse(toolAskResponse) {
      this.addMessage('bot', toolAskResponse.answer, toolAskResponse.tools ?? [])
    },

    handleAskError(error) {
      this.addMessage('bot', error.response?.data?.message ?? REQUEST_FAILED, [], true)
      if (error.response?.status === 401) {
        loadSession()
      }
    },

    addMessage(sender, text, tools = [], isError = false) {
      this.messages.push({ sender, text, tools, isError })
    },

    openTool(toolId) {
      NavigationService.navigateToToolDetail(this.$router, toolId)
    },

    handleScroll() {
      const messageList = this.$refs.messageList
      this.isAtBottom =
        messageList.scrollHeight - messageList.scrollTop - messageList.clientHeight < 8
    },

    scrollToBottomAfterRender() {
      this.$nextTick(() => this.scrollToBottom())
    },

    scrollToBottom() {
      const messageList = this.$refs.messageList
      if (!messageList) return
      messageList.scrollTop = messageList.scrollHeight
      this.isAtBottom = true
    },
  },
}
</script>

<style scoped>
.chatbot-toggle {
  position: fixed;
  right: var(--space-5);
  bottom: var(--space-5);
  z-index: 1050;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: var(--radius-pill);
  box-shadow: var(--shadow-card-hover);
  display: flex;
  align-items: center;
  justify-content: center;
}

.chatbot-window {
  position: fixed;
  right: var(--space-5);
  bottom: var(--space-5);
  z-index: 1050;
  width: 23rem;
  height: min(32rem, calc(100vh - 3rem));
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-panel);
  box-shadow: var(--shadow-card-hover);
  overflow: hidden;
}

.chatbot-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-primary);
  color: #fff;
}

.chatbot-title {
  flex: 1;
  margin: 0;
  font-size: var(--font-size-lead);
}

.chatbot-close {
  border: 0;
  background: transparent;
  color: inherit;
  padding: var(--space-1);
  line-height: 0;
}

.chatbot-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  background: var(--color-canvas);
}

.chatbot-message {
  align-self: flex-start;
  max-width: 85%;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-card);
  background: var(--color-surface-soft);
  color: var(--color-ink);
  white-space: pre-line;
}

.chatbot-message-user {
  align-self: flex-end;
  background: var(--color-primary);
  color: #fff;
}

.chatbot-message-error {
  background: #f8d7da;
  color: #842029;
}

.chatbot-message-pending {
  color: var(--color-muted);
  font-style: italic;
}

.chatbot-tools {
  margin: var(--space-2) 0 0;
  padding-left: var(--space-4);
  font-size: var(--font-size-small);
}

.chatbot-tools a {
  color: var(--color-primary-active);
}

.chatbot-scroll-down {
  position: absolute;
  left: 50%;
  bottom: 4.5rem;
  transform: translateX(-50%);
  width: 2.5rem;
  height: 2.5rem;
  border: 0;
  border-radius: var(--radius-pill);
  background: rgb(41 53 47 / 45%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chatbot-input {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3);
  border-top: 1px solid var(--color-border);
}

.chatbot-input textarea {
  resize: none;
  field-sizing: content;
  max-height: 6rem;
}

@media (max-width: 575.98px) {
  .chatbot-window {
    right: var(--space-2);
    left: var(--space-2);
    bottom: var(--space-2);
    width: auto;
    height: calc(100dvh - 5rem);
  }
}
</style>
