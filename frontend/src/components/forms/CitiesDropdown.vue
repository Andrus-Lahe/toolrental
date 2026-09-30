<template>
  <select
    id="city"
    class="form-select"
    :value="selectedCityId"
    @change="$emit('event-new-city-selected', Number($event.target.value))"
  >
    <option value="0">{{ firstOptionLabel }}</option>
    <option v-for="city in cities" :key="city.cityId" :value="city.cityId">
      {{ city.cityName }}
    </option>
  </select>
</template>

<script>
export default {
  name: 'CitiesDropdown',
  props: {
    cities: Array,
    selectedCityId: Number,
    firstOptionLabel: {
      type: String,
      default: 'Kõik linnad',
    },
  },
  emits: ['event-new-city-selected'],
}
</script>
