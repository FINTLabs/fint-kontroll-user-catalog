package no.fintlabs.user;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


@Configuration
public class UserConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, FactoryUser> userConsumer(
            UserService userService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("user")
                .build();

        return entityConsumerFactoryService.createFactory(
                        FactoryUser.class,
                        (ConsumerRecord<String, FactoryUser> consumerRecord)
                                -> userService.save(consumerRecord.key(), consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }
}
