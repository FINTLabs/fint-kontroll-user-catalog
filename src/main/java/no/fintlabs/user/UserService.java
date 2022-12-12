package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.User;
import no.fintlabs.repository.UserRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Flux<User> getAllUsers(FintJwtEndUserPrincipal principal) {
        List<User> allUsers  = userRepository .findAll().stream().collect(Collectors.toList());

        return Flux.fromIterable(allUsers);
    }

    public Stream<User> getAllUsersStream(FintJwtEndUserPrincipal principal) {


        return userRepository .findAll().stream();
    }
    public Mono<User> getById(FintJwtEndUserPrincipal from, String id) {
        User user = userRepository.findById(id).orElse(new User());

        return Mono.just(user);

    }

    public Flux<User> getAllUsersByUserType(FintJwtEndUserPrincipal from, String usertype) {
        List<User> allUsers = userRepository.findByUserType(usertype);

        return Flux.fromIterable(allUsers);
    }

    public Flux<User> getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal from, String firstnamepart) {
        List<User> allUsers = userRepository.findAllUsersByStartingFirstnameWith(firstnamepart);
        return Flux.fromIterable(allUsers);
    }
}
