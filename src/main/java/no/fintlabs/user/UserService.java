package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.MemberService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    private final MemberService memberService;


    public UserService(UserRepository userRepository, MemberService memberService) {
        this.userRepository = userRepository;
        this.memberService = memberService;
    }

    public void save(User user) {

        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.getResourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));


    }

    private Runnable onSaveNewUser(User user) {
        return () -> {
            User newUser = userRepository.save(user);
            memberService.process(memberService.create(newUser));
        };
    }

    private Consumer<User> onSaveExistingUser(User user) {
        return existingUser -> {
            user.setId(existingUser.getId());
            memberService.process(memberService.create(user));
            userRepository.save(user);
        };
    }

    public Mono<DetailedUser> getDetailedUserById(
            FintJwtEndUserPrincipal principal,
            Long id) {
        return Mono.just(userRepository.findById(id)
                .map(User::toDetailedUser).orElse(new DetailedUser()));
    }


    public List<SimpleUser> getSimpleUsers(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            String userType ) {



        return null;
    }

}
