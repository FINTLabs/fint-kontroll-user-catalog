package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEntityProducerService {
    private final EntityProducer<User> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public UserEntityProducerService(EntityProducerFactory entityProducerFactory,
                                     EntityTopicService entityTopicService) {
        entityProducer = entityProducerFactory.createProducer(User.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("kontrolluser")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters,0);
    }

    public void publish(User user){
        String key = user.getResourceId();
        entityProducer.send(
                EntityProducerRecord.<User>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(user)
                        .build()
        );
    }



}
