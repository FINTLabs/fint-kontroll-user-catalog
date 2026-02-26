package no.fintlabs.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteUserJob {

    private final UserRepository userRepository;

    @Value("${jobs.delete-user.deleted-since-days}")
    private long deletedSinceDays;

    @Scheduled(fixedDelayString = "${jobs.delete-user.interval-cron}")
    @Transactional
    public void deleteUsersMarkedDeletedLongEnough() {

        Instant cutoffInstant = Instant.now().minus(deletedSinceDays, ChronoUnit.DAYS);
        Date cutoffDate = Date.from(cutoffInstant);

        int deletedCount =
                userRepository.deleteByStatusAndStatusChangedBefore("DELETED", cutoffDate);

        log.info(
                "DeleteUserJob removed {} users with status=DELETED older than {} days",
                deletedCount,
                deletedSinceDays
        );
    }
}