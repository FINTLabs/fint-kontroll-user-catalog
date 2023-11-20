package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class MemberEntityProducerService {
    private final EntityProducer<Member> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    private final EntityTopicService entityTopicService;


    public MemberEntityProducerService(EntityProducerFactory entityProducerFactory,
                                       EntityTopicService entityTopicService) {
        entityProducer = entityProducerFactory.createProducer(Member.class);
        this.entityTopicService = entityTopicService;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("member")
                .build();
    }

    @PostConstruct
    public void init() {
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public void publish(Member member) {
        String key = member.getResourceId();
        log.info("Publish member : " + member.getResourceId());
        entityProducer.send(
                EntityProducerRecord.<Member>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(member)
                        .build()
        );
    }
}
