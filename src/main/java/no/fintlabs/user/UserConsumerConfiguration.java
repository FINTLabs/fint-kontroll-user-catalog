package no.fintlabs.user;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.model.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


@Configuration
public class UserConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, User> userConsumer(
            UserService userService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("entitytouser")
                .build();

        ConcurrentMessageListenerContainer container = entityConsumerFactoryService.createFactory(
                User.class,
                (ConsumerRecord<String,User> consumerRecord)
                -> userService.save(consumerRecord.value()))
                        .createContainer(entityTopicNameParameters);

        return container;

    }


}
