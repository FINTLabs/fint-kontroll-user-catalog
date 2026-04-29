package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class UserEntityProducerService {
    private final ParameterizedTemplate<User> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public UserEntityProducerService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(User.class);
        entityTopicNameParameters =  EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault().build())
                .resourceName("kontrolluser")
                .build();

        entityTopicService.createOrModifyTopic(entityTopicNameParameters, EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publish(User user){
        String key = user.getResourceId();
        log.info("Publishing user with resourceId: {}", user.getResourceId());
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<User>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(user)
                        .build()
        );
    }

    public int publishKontrollUsers(String triggerType, List<User> users){
        log.info("Republishing all {} kontrollusers triggered by {}", users.size(), triggerType);

        users.forEach(this::publish);
        log.info("Republishing all {} kontrollusers triggered by {} done", users.size(), triggerType);
        return users.size();
    }
}
