package no.fintlabs.member;

public class MemberService {
    private final MemberEntityProducerService memberEntityProducerService;

    public MemberService(MemberEntityProducerService memberEntityProducerService) {
        this.memberEntityProducerService = memberEntityProducerService;
    }

    public void process(Member member){
        memberEntityProducerService.publish(member);
    }
}
