package no.fintlabs.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusReconciliationJob {

    private final UserService userService;

    @Scheduled(cron = "${fint.kontroll.user.status-reconciliation.interval-cron}")
    public void reconcileTimeBasedUserStatuses() {
        int changedUsers = userService.reconcileTimeBasedUserStatuses().size();
        log.info("UserStatusReconciliationJob updated {} time-based user statuses", changedUsers);
    }
}
