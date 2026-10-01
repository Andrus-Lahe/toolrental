<template>
  <select
    class="form-select"
    :disabled="isDisabled"
    @change="$emit('event-new-district-selected', Number($event.target.value))"
  >
    <option :value="0" :selected="selectedDistrictId === 0">{{ firstOptionLabel }}</option>
    <option
      v-for="district in districts"
      :key="district.districtId"
      :value="district.districtId"
      :selected="district.districtId === selectedDistrictId"
    >
      {{ district.districtName }}
    </option>
  </select>
</template>

<script>
export default {
  name: 'DistrictsDropdown',
  props: {
    districts: {
      type: Array,
      default: () => [],
    },
    selectedDistrictId: {
      type: Number,
      default: 0,
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
    firstOptionLabel: {
      type: String,
      default: 'Kõik linnaosad',
    },
  },
  emits: ['event-new-district-selected'],
}
</script>
