package no.fintlabs.externalUser;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.user.FactoryUser;
import no.fintlabs.user.UserService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalUserService {

    private final UserService userService;

    public ExternalUserService(UserService userService) {
        this.userService = userService;
    }

    public void convertAndSaveAsUser(String key, ExternalUserPayload externalUser) {
        if(externalUser == null) {
            userService.markUserDeleted(key);
            return;
        }
        FactoryUser convertedExternalUser = externalUser.toFactoryUser(key);
        userService.save(convertedExternalUser.resourceId(), convertedExternalUser);
    }
}
