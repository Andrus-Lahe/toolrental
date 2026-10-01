<template>
  <main class="container py-5">
    <h1>Minu profiil</h1>

    <form class="profile-form mt-4" novalidate @submit.prevent="saveProfile">
      <AlertDanger :error-message="errorMessage" />

      <div class="mb-3">
        <label for="firstName" class="form-label">Eesnimi*</label>
        <input
          id="firstName"
          v-model="profile.firstName"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.firstName) }"
          maxlength="100"
        />
      </div>
      <div class="mb-3">
        <label for="lastName" class="form-label">Perenimi*</label>
        <input
          id="lastName"
          v-model="profile.lastName"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.lastName) }"
          maxlength="100"
        />
      </div>
      <div class="mb-3">
        <label for="email" class="form-label">E-post*</label>
        <input
          id="email"
          v-model="profile.email"
          type="email"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.email) }"
          maxlength="254"
        />
      </div>
      <div class="mb-3">
        <label for="phone" class="form-label">Telefon*</label>
        <input
          id="phone"
          v-model="profile.phone"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.phone) }"
          maxlength="32"
        />
      </div>
      <div class="mb-3">
        <label class="form-label">Linn*</label>
        <CitiesDropdown
          first-option-label="Vali linn"
          :cities="cities"
          :selected-city-id="selectedCityId"
          :class="{ 'is-invalid': shouldHighlightMissingFields && selectedCityId === 0 }"
          @event-new-city-selected="handleCitySelected"
        />
      </div>

      <div class="mb-3">
        <label class="form-label">Linnaosa*</label>
        <DistrictsDropdown
          first-option-label="Vali linnaosa"
          :districts="districts"
          :selected-district-id="profile.districtId"
          :is-disabled="isDistrictsDisabled"
          :class="{ 'is-invalid': shouldHighlightMissingFields && profile.districtId === 0 }"
          @event-new-district-selected="handleDistrictSelected"
        />
      </div>
      <div class="mb-3">
        <label for="streetName" class="form-label">Tänava nimi*</label>
        <input
          id="streetName"
          v-model="profile.streetName"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.streetName) }"
          maxlength="150"
        />
      </div>
      <div class="mb-3">
        <label for="houseNumber" class="form-label">Majanumber*</label>
        <input
          id="houseNumber"
          v-model="profile.houseNumber"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': isRequiredTextMissing(profile.houseNumber) }"
          maxlength="20"
        />
      </div>
      <div class="mb-4">
        <label for="apartmentNumber" class="form-label">Korteri number (valikuline)</label>
        <input
          id="apartmentNumber"
          v-model="profile.apartmentNumber"
          type="text"
          class="form-control"
          maxlength="20"
        />
      </div>

      <button type="submit" class="btn btn-primary" :disabled="isLoading || isSaving">
        Salvesta
      </button>
    </form>

    <RequiredFieldsModal
      :is-open="isRequiredFieldsModalOpen"
      @event-modal-closed="handleRequiredFieldsModalClosed"
    />
  </main>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import RequiredFieldsModal from '@/components/modals/RequiredFieldsModal.vue'
import CitiesDropdown from '@/components/forms/CitiesDropdown.vue'
import DistrictsDropdown from '@/components/forms/DistrictsDropdown.vue'
import ProfileService from '@/api-services/ProfileService.js'
import CityService from '@/api-services/CityService.js'
import NavigationService from '@/navigation/NavigationService.js'
import { loadSession, markProfileCompleted, session } from '@/auth/session.js'

const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'

export default {
  name: 'MyProfile',
  components: { AlertDanger, CitiesDropdown, DistrictsDropdown, RequiredFieldsModal },
  data() {
    return {
      errorMessage: '',
      hasAttemptedSave: false,
      isRequiredFieldsModalOpen: false,
      isLoading: true,
      isSaving: false,
      cities: [],
      districts: [],
      selectedCityId: 0,
      profile: {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        districtId: 0,
        streetName: '',
        houseNumber: '',
        apartmentNumber: '',
      },
    }
  },
  computed: {

    shouldHighlightMissingFields() {
      return this.hasAttemptedSave || this.isCompletingProfile
    },
    isCompletingProfile() {
      return this.$route.query.completeProfile === 'true'
    },

    isFormValid() {
      const { firstName, lastName, email, phone, streetName, houseNumber, districtId } =
        this.profile
      const requiredTexts = [firstName, lastName, email, phone, streetName, houseNumber]
      return (
        requiredTexts.every((text) => text.trim() !== '') &&
        this.selectedCityId !== 0 &&
        districtId !== 0
      )
    },

    isDistrictsDisabled() {
      return this.selectedCityId === 0
    },
  },
  beforeMount() {
    this.isRequiredFieldsModalOpen = this.isCompletingProfile
    this.getMyProfile()
    this.getCities()
  },
  methods: {
    handleRequiredFieldsModalClosed() {
      this.isRequiredFieldsModalOpen = false
    },

    isRequiredTextMissing(value) {
      return this.shouldHighlightMissingFields && value.trim() === ''
    },

    getMyProfile() {
      ProfileService.sendGetMyProfileRequest()
        .then((response) => this.handleGetMyProfileResponse(response.data))
        .catch((error) => this.handleApiError(error))
        .finally(() => (this.isLoading = false))
    },

    handleGetMyProfileResponse(profileDto) {
      this.profile.firstName = profileDto.firstName ?? ''
      this.profile.lastName = profileDto.lastName ?? ''
      this.profile.email = profileDto.email ?? ''
      this.profile.phone = profileDto.phone ?? ''
      this.profile.districtId = profileDto.districtId ?? 0
      this.profile.streetName = profileDto.streetName ?? ''
      this.profile.houseNumber = profileDto.houseNumber ?? ''
      this.profile.apartmentNumber = profileDto.apartmentNumber ?? ''
      this.selectedCityId = profileDto.cityId ?? 0
      if (this.selectedCityId !== 0) {
        this.getCityDistricts()
      }
    },

    getCities() {
      CityService.sendGetCitiesRequest()
        .then((response) => (this.cities = response.data))
        .catch((error) => this.handleApiError(error))
    },

    handleCitySelected(cityId) {
      this.selectedCityId = cityId
      this.profile.districtId = 0
      this.districts = []
      if (cityId !== 0) {
        this.getCityDistricts()
      }
    },

    handleDistrictSelected(districtId) {
      this.profile.districtId = districtId
    },

    saveProfile() {
      if (this.isLoading || this.isSaving) {
        return
      }
      this.errorMessage = ''
      this.hasAttemptedSave = true
      if (!this.isFormValid) {
        this.isRequiredFieldsModalOpen = true
        return
      }
      this.isSaving = true
      ProfileService.sendPutMyProfileRequest(this.createProfileRequest())
        .then(() => this.handleSaveProfileResponse())
        .catch((error) => this.handleApiError(error))
        .finally(() => (this.isSaving = false))
    },

    createProfileRequest() {
      return {
        firstName: this.profile.firstName.trim(),
        lastName: this.profile.lastName.trim(),
        email: this.profile.email.trim(),
        phone: this.profile.phone.trim(),
        districtId: this.profile.districtId,
        streetName: this.profile.streetName.trim(),
        houseNumber: this.profile.houseNumber.trim(),
        apartmentNumber: this.profile.apartmentNumber.trim() || null,
      }
    },

    // Ühine kasutajaolek värskendatakse, et päis ei suunaks kasutajat enam profiili täitma.
    handleSaveProfileResponse() {
      markProfileCompleted()
      loadSession()
      NavigationService.navigateToHomeView(this.$router)
    },

    getCityDistricts() {
      const requestedCityId = this.selectedCityId
      CityService.sendGetCityDistrictsRequest(requestedCityId)
        .then((response) => this.handleGetCityDistrictsResponse(requestedCityId, response.data))
        .catch((error) => this.handleGetCityDistrictsError(requestedCityId, error))
    },

    // Aegunud vastus (kasutaja on vahepeal teise linna valinud) jäetakse tähelepanuta.
    handleGetCityDistrictsResponse(requestedCityId, districts) {
      if (requestedCityId === this.selectedCityId) {
        this.districts = districts
      }
    },

    handleGetCityDistrictsError(requestedCityId, error) {
      if (requestedCityId === this.selectedCityId) {
        this.districts = []
        this.handleApiError(error)
      }
    },

    handleApiError(error) {
      if (!error.response) {
        this.errorMessage = NETWORK_ERROR_MESSAGE
      } else if (error.response.status === 401) {
        session.user = null
        session.status = 'guest'
        NavigationService.navigateToHomeView(this.$router)
      } else {
        this.errorMessage = error.response.data?.message ?? NETWORK_ERROR_MESSAGE
      }
    },
  },
}
</script>

<style scoped>
.profile-form {
  max-width: 520px;
}
</style>
