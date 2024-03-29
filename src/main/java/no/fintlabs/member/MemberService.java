package no.fintlabs.member;

import no.fintlabs.user.User;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberEntityProducerService memberEntityProducerService;

    public MemberService(MemberEntityProducerService memberEntityProducerService) {
        this.memberEntityProducerService = memberEntityProducerService;
    }

    public void process(Member member) {
        memberEntityProducerService.publish(member);
    }

    public Member create(User user) {
        return Member.builder()
                .id(user.getId())
                .resourceId(user.getResourceId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .UserName(user.getUserName())
                .identityProviderUserObjectId(user.getIdentityProviderUserObjectId())
                .organisationUnitId(user.getMainOrganisationUnitId())
                .organisationUnitName(user.getMainOrganisationUnitName())
                .build();
    }
}
