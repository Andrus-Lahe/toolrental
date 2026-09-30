<template>
  <select
    id="district"
    class="form-select"
    :value="selectedDistrictId"
    :disabled="isDisabled"
    @change="$emit('event-new-district-selected', Number($event.target.value))"
  >
    <option value="0">{{ firstOptionLabel }}</option>
    <option v-for="district in districts" :key="district.districtId" :value="district.districtId">
      {{ district.districtName }}
    </option>
  </select>
</template>

<script>
export default {
  name: 'DistrictsDropdown',
  props: {
    districts: Array,
    selectedDistrictId: Number,
    isDisabled: Boolean,
    firstOptionLabel: {
      type: String,
      default: 'Kõik linnaosad',
    },
  },
  emits: ['event-new-district-selected'],
}
</script>
