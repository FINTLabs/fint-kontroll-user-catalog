package no.fintlabs.externalUser;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ExternalUserConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ExternalUserPayload> externalUserConsumer(
            ExternalUserService externalUserService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("graph-user-external")
                .build();

        return entityConsumerFactoryService.createFactory(
                        ExternalUserPayload.class,
                        (ConsumerRecord<String,ExternalUserPayload> consumerRecord)
                                -> externalUserService.convertAndSaveAsUser(consumerRecord.key(), consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }

}
