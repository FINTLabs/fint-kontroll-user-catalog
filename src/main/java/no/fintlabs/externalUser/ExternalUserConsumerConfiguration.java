package no.fintlabs.externalUser;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ExternalUserConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ExternalUser> externalUserConsumer(
            ExternalUserService externalUserService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("externaluser")
                .build();

        return entityConsumerFactoryService.createFactory(
                        ExternalUser.class,
                        (ConsumerRecord<String,ExternalUser> consumerRecord)
                                -> externalUserService.convertAndSaveAsUser(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }

}
