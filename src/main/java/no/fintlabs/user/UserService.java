package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.MemberService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    private final MemberService memberService;

    private final ResponseFactory responseFactory;


    public UserService(UserRepository userRepository, MemberService memberService, ResponseFactory responseFactory) {
        this.userRepository = userRepository;
        this.memberService = memberService;
        this.responseFactory = responseFactory;
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
                .map(UserFactory::toDetailedUser).orElse(new DetailedUser()));
    }


    public ResponseEntity<Map<String, Object>> getAllUserPagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<SimpleUser> userDTOforListPage;
        userDTOforListPage = userRepository.findAll(paging)
                .map(UserFactory::toSimpleUser);

        return responseFactory.toResponseEntity(userDTOforListPage);
    }

    public ResponseEntity<Map<String, Object>> getAllUserByTypePagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size,
            String usertype) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<SimpleUser> userDTOforListPage = userRepository.findUsersByUserTypeEquals(paging, usertype)
                .map(UserFactory::toSimpleUser);
        return responseFactory.toResponseEntity(userDTOforListPage);
    }





}
