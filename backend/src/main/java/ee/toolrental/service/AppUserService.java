package ee.toolrental.service;

import ee.toolrental.controller.appuser.dto.CurrentUserDto;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserMapper;
import ee.toolrental.persistence.appuser.AppUserRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final ProfileRepository profileRepository;

    // Tagastab sisselogitud kasutaja andmed teenusele GET /api/me (ID, roll, nimi, e-post, hasProfile).
    // Kõigepealt leitakse kasutaja andmebaasist, mapper teisendab ta DTO-ks (andmekast frontendile)
    // ja lõpuks lisab handleProfile e-posti ning märgib, kas kasutajal on profiil juba täidetud.
    public CurrentUserDto getCurrentUser(Integer userId, String email) {
        AppUser appUser = getValidAppUserBy(userId);
        CurrentUserDto currentUserDto = appUserMapper.toCurrentUserDto(appUser);
        Optional<Profile> optionalProfile = profileRepository.findProfileBy(userId);
        handleProfile(currentUserDto, email, optionalProfile);
        return currentUserDto;
    }

    // Otsib kasutaja ID järgi app_user tabelist. Nimi getValid... lubab, et tagastatakse alati päris kasutaja.
    // Kui sellise ID-ga kasutajat pole, visatakse PrimaryKeyNotFoundException, millest saab 404 vastus.
    // Seda meetodit kasutavad ka teised service'id (nt ProfileService), et sama kontrolli mitte korrata.
    public AppUser getValidAppUserBy(Integer userId) {
        return appUserRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    // Täidab DTO-s e-posti ja hasProfile välja vastavalt sellele, kas kasutajal on profiil olemas.
    // Optional on "karp", mis võib olla tühi: profiiliga kasutajal võetakse e-post profiilist (hasProfile = true),
    // profiilita kasutajal Google'i sessioonist (hasProfile = false). handle-prefiks tähendab, et meetod muudab DTO-d.
    private void handleProfile(CurrentUserDto currentUserDto, String email, Optional<Profile> optionalProfile) {
        if (optionalProfile.isPresent()) {
            currentUserDto.setEmail(optionalProfile.get().getEmail());
            currentUserDto.setHasProfile(true);
        } else {
            currentUserDto.setEmail(email);
            currentUserDto.setHasProfile(false);
        }
    }

}



