package ee.toolrental.controller.appuser;

import ee.toolrental.infrastructure.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppUserController {
    @GetMapping("/me")
    public void getCurrentUser(@AuthenticationPrincipal AppUserPrincipal principal) {
        return appUserService.getCurrentUser(principal.getUserId(), principal.getEmail());)

    }
}
