package no.fintlabs.externalUser;

import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ExternalUserConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ExternalUserPayload> externalUserConsumer(
            ExternalUserService externalUserService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            ErrorHandlerFactory errorHandlerFactory
    ){
        ListenerConfiguration listenerConfiguration = ListenerConfiguration
                .stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .continueFromPreviousOffsetOnAssignment()
                .build();


        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ExternalUserPayload.class,
                        (ConsumerRecord<String,ExternalUserPayload> consumerRecord)
                                -> externalUserService.convertAndSaveAsUser(consumerRecord.key(), consumerRecord.value()),
                        listenerConfiguration,
                        errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build())
                )
                .createContainer(EntityTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters
                                .stepBuilder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build())
                        .resourceName("graph-user-external")
                        .build());

    }

}
