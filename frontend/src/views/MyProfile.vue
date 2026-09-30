<template>
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <h1 class="mb-4">Minu profiil</h1>

        <AlertDanger :error-message="errorMessage" />

        <div class="mb-3">
          <label for="firstName" class="form-label">Eesnimi</label>
          <input
            id="firstName"
            v-model="profile.firstName"
            type="text"
            class="form-control"
            maxlength="100"
          />
        </div>

        <div class="mb-3">
          <label for="lastName" class="form-label">Perenimi</label>
          <input
            id="lastName"
            v-model="profile.lastName"
            type="text"
            class="form-control"
            maxlength="100"
          />
        </div>

        <div class="mb-3">
          <label for="email" class="form-label">E-post</label>
          <input
            id="email"
            v-model="profile.email"
            type="email"
            class="form-control"
            maxlength="254"
          />
        </div>

        <div class="mb-3">
          <label for="phone" class="form-label">Telefon</label>
          <input
            id="phone"
            v-model="profile.phone"
            type="text"
            class="form-control"
            maxlength="32"
          />
        </div>

        <div class="mb-3">
          <label for="streetName" class="form-label">Tänava nimi</label>
          <input
            id="streetName"
            v-model="profile.streetName"
            type="text"
            class="form-control"
            maxlength="150"
          />
        </div>

        <div class="mb-3">
          <label for="houseNumber" class="form-label">Majanumber</label>
          <input
            id="houseNumber"
            v-model="profile.houseNumber"
            type="text"
            class="form-control"
            maxlength="20"
          />
        </div>

        <div class="mb-3">
          <label for="apartmentNumber" class="form-label">Korteri number (valikuline)</label>
          <input
            id="apartmentNumber"
            v-model="profile.apartmentNumber"
            type="text"
            class="form-control"
            maxlength="20"
          />
        </div>

        <div class="mb-3">
          <label for="district" class="form-label">Linnaosa</label>
          <DistrictsDropdown
            first-option-label="Vali linnaosa"
            :districts="districts"
            :selected-district-id="profile.districtId"
            :is-disabled="isDistrictsDisabled"
            @event-new-district-selected="handleDistrictSelected"
          />
        </div>

        <div class="mb-4">
          <label for="city" class="form-label">Linn</label>
          <CitiesDropdown
            first-option-label="Vali linn"
            :cities="cities"
            :selected-city-id="selectedCityId"
            @event-new-city-selected="handleCitySelected"
          />
        </div>

        <button class="btn btn-primary" :disabled="isLoading || isSaving" @click="saveProfile">
          Salvesta
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import AlertDanger from '@/components/common/AlertDanger.vue'
import CitiesDropdown from '@/components/forms/CitiesDropdown.vue'
import DistrictsDropdown from '@/components/forms/DistrictsDropdown.vue'
import ProfileService from '@/api-services/ProfileService.js'
import CityService from '@/api-services/CityService.js'
import NavigationService from '@/navigation/NavigationService.js'
import userState from '@/auth/UserState.js'

export default {
  name: 'MyProfile',
  components: { AlertDanger, CitiesDropdown, DistrictsDropdown },
  data() {
    return {
      errorMessage: '',
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
    isFormValid() {
      const requiredFields = [
        this.profile.firstName,
        this.profile.lastName,
        this.profile.email,
        this.profile.phone,
        this.profile.streetName,
        this.profile.houseNumber,
      ]
      return (
        requiredFields.every((field) => field.trim() !== '') &&
        this.selectedCityId !== 0 &&
        this.profile.districtId !== 0
      )
    },

    isDistrictsDisabled() {
      return this.selectedCityId === 0
    },
  },
  methods: {
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
        .finally()
    },

    getCityDistricts() {
      CityService.sendGetCityDistrictsRequest(this.selectedCityId)
        .then((response) => (this.districts = response.data))
        .catch((error) => this.handleGetCityDistrictsError(error))
        .finally()
    },

    handleGetCityDistrictsError(error) {
      this.districts = []
      this.handleApiError(error)
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
      this.errorMessage = ''
      if (!this.isFormValid) {
        this.errorMessage = 'Täida kõik kohustuslikud väljad'
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

    handleSaveProfileResponse() {
      // Värskenda ühist kasutajaolekut, et hasProfile oleks true ja päis ei suunaks uuesti profiilile
      userState.loadCurrentUser().then(() => NavigationService.navigateToHomeView())
    },

    handleApiError(error) {
      if (!error.response) {
        this.errorMessage = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
      } else if (error.response.status === 401) {
        NavigationService.navigateToHomeView()
      } else {
        this.errorMessage =
          error.response.data?.message ?? 'Toiming ebaõnnestus. Palun proovi hiljem uuesti.'
      }
    },
  },
  beforeMount() {
    this.getMyProfile()
    this.getCities()
  },
}
</script>
