package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserPublishingComponent {
    private final UserEntityProducerService userEntityProducerService;
    private final UserService userService;

    public UserPublishingComponent(UserEntityProducerService userEntityProducerService, UserService userService) {
        this.userEntityProducerService = userEntityProducerService;
        this.userService = userService;
    }
    @Scheduled(
            initialDelayString = "${fint.kontroll.user-catalog.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.user-catalog.publishing.fixed-delay}",
            timeUnit = TimeUnit.HOURS
    )
    public void publishUsers() {
        String triggerType = "scheduled job";
        List<User> allUsers = userService.getAllUsers();

        if (allUsers.isEmpty()) {
            log.info("No users to publish");
            return;
        }
        userEntityProducerService.publishKontrollUsers(triggerType, allUsers);
    }
}
