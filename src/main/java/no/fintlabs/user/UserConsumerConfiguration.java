package no.fintlabs.user;


import lombok.extern.slf4j.Slf4j;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.OffsetSeekingTrigger;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class UserConsumerConfiguration {

    @Bean
    public OffsetSeekingTrigger userOffsetSeekingTrigger() {
        return new OffsetSeekingTrigger();
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, FactoryUser> userConsumer(
            UserService userService,
            ErrorHandlerFactory errorHandlerFactory,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            OffsetSeekingTrigger userOffsetSeekingTrigger
    ) {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration
                .stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .continueFromPreviousOffsetOnAssignment()
                .offsetSeekingTrigger(userOffsetSeekingTrigger)
                .build();

        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                FactoryUser.class,
                (ConsumerRecord<String, FactoryUser> consumerRecord)
                        -> {
                    log.info("Received user from kafka: {}", consumerRecord.key());
                    userService.save(consumerRecord.key(), consumerRecord.value());},
                listenerConfiguration,
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build())
        ).createContainer(
                EntityTopicNameParameters.builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters.stepBuilder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build())
                        .resourceName("user")
                        .build());

    }
}
