<template>
  <div class="table-responsive">
    <table class="table table-striped align-middle">
      <thead>
        <tr>
          <th scope="col">Nimi</th>
          <th scope="col">E-post</th>
          <th scope="col">Roll</th>
          <th scope="col">Registreeritud</th>
          <th scope="col">Olek</th>
          <th scope="col">Tegevus</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.userId">
          <td>{{ formatName(user) }}</td>
          <td>{{ user.email ?? '—' }}</td>
          <td>{{ formatRole(user.roleName) }}</td>
          <td>{{ formatDate(user.registeredAt) }}</td>
          <td>{{ formatStatus(user.status) }}</td>
          <td>
            <div class="d-flex gap-2">
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                :disabled="pendingUserId === user.userId"
                @click="$emit('event-block-user', user.userId)"
              >
                Blokeeri
              </button>
              <button
                type="button"
                class="btn btn-outline-danger btn-sm"
                :disabled="pendingUserId === user.userId"
                @click="$emit('event-delete-user', user)"
              >
                Kustuta
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'AdminUsersTable',
  props: {
    users: {
      type: Array,
      default: () => [],
    },
    pendingUserId: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-block-user', 'event-delete-user'],
  methods: {
    formatName(user) {
      return [user.firstName, user.lastName]
        .map((name) => (name ?? '').trim())
        .filter((name) => name !== '')
        .join(' ')
    },

    formatRole(roleName) {
      return roleName === 'customer' ? 'kasutaja' : (roleName ?? '—')
    },

    // Kuupäev tuleb kujul YYYY-MM-DD; sõne jagatakse, et ajavööndi teisendus päeva ei nihutaks.
    formatDate(registeredAt) {
      if (!registeredAt) return '—'
      const [year, month, day] = registeredAt.split('-')
      return `${day}.${month}.${year}`
    },

    formatStatus(status) {
      return status === 'B' ? 'Mitteaktiivne' : 'Aktiivne'
    },
  },
}
</script>
