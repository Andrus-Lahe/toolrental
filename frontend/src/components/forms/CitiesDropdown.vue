<template>
  <select
    class="form-select"
    @change="$emit('event-new-city-selected', Number($event.target.value))"
  >
    <option :value="0" :selected="selectedCityId === 0">{{ firstOptionLabel }}</option>
    <option
      v-for="city in cities"
      :key="city.cityId"
      :value="city.cityId"
      :selected="city.cityId === selectedCityId"
    >
      {{ city.cityName }}
    </option>
  </select>
</template>

<script>
export default {
  name: 'CitiesDropdown',
  props: {
    cities: {
      type: Array,
      default: () => [],
    },
    selectedCityId: {
      type: Number,
      default: 0,
    },
    firstOptionLabel: {
      type: String,
      default: 'Kõik linnad',
    },
  },
  emits: ['event-new-city-selected'],
}
</script>
