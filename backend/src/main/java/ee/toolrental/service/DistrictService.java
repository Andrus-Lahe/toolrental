package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.district.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DistrictService {

    private final DistrictRepository districtRepository;

    /**
     * Leiab linnaosa ID järgi.
     * Kui linnaosa pole, visatakse PrimaryKeyNotFoundException (404 PRIMARY_KEY_NOT_FOUND).
     */
    public District getValidDistrictBy(Integer districtId) {
        return districtRepository.findById(districtId).orElseThrow(() -> new PrimaryKeyNotFoundException("districtId", districtId));
    }

}
