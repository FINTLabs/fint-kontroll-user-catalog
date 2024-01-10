package no.fintlabs.externalUser;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalUserService {

    private final UserService userService;

    public ExternalUserService(UserService userService) {
        this.userService = userService;
    }


    public void convertAndSaveAsUser(ExternalUser externalUser) {
        User convertedExternalUser = externalUser.toUser();
        userService.save(convertedExternalUser);
    }
}
