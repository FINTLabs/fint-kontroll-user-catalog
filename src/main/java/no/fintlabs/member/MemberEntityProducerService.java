package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MemberEntityProducerService {
    private final EntityProducer<Member> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    public MemberEntityProducerService( EntityProducerFactory entityProducerFactory)
    {
        entityProducer = entityProducerFactory.createProducer(Member.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("member")
                .build();
    }

    public void publish(Member member) {
        String key = member.getResourceId();
        entityProducer.send(
                EntityProducerRecord.<Member>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(member)
                        .build()
        );
    }
}
