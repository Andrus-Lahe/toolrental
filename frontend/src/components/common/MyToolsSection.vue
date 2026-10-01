<template>
  <section class="mb-4" :aria-labelledby="headingId">
    <h3 :id="headingId" class="h5 mb-3">{{ title }}</h3>
    <p v-if="isLoaded && items.length === 0" class="text-muted mb-0">Tööriistu ei ole</p>
    <div v-else-if="isLoaded" class="row row-cols-1 row-cols-sm-2 row-cols-xl-3 g-3">
      <div v-for="item in items" :key="getItemKey(item)" class="col">
        <MyToolCard
          :tool="item"
          :is-details-disabled="isDetailsDisabled"
          :is-deletable="isDeletable"
          :is-delete-disabled="isDeleteDisabled"
          @event-view-details="$emit('event-view-details', item)"
          @event-delete-tool="$emit('event-delete-tool', item)"
        />
      </div>
    </div>
  </section>
</template>

<script>
import MyToolCard from '@/components/common/MyToolCard.vue'

export default {
  name: 'MyToolsSection',
  components: { MyToolCard },
  props: {
    title: { type: String, required: true },
    headingId: { type: String, required: true },
    items: { type: Array, required: true },
    itemType: { type: String, required: true },
    isLoaded: { type: Boolean, default: false },
    isDetailsDisabled: { type: Boolean, default: false },
    isDeletable: { type: Boolean, default: false },
    isDeleteDisabled: { type: Boolean, default: false },
  },
  emits: ['event-view-details', 'event-delete-tool'],
  methods: {
    getItemKey(item) {
      return this.itemType === 'booking' ? item.bookingId : item.toolId
    },
  },
}
</script>
