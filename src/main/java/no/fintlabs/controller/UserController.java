package no.fintlabs.controller;


import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.model.User;
import no.fintlabs.repository.UserRepository;
import no.fintlabs.user.UserService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final FintFilterService fintFilterService;

    public UserController(UserService userService, FintFilterService fintFilterService) {
        this.userService = userService;
        this.fintFilterService = fintFilterService;
    }

    @GetMapping
    public Flux<User> getAllUsers(@AuthenticationPrincipal Jwt jwt){
        log.info("Fetching all users");

        return userService.getAllUsers(FintJwtEndUserPrincipal.from(jwt));
    }

    @GetMapping(params = "$filter")
    public Flux<User> getUsersByFilter(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(value = "$filter", required = false) String filter){
        log.info("Seaching all users with filter : " + filter);
        Stream<User> allUsers = userService.getAllUsersStream(FintJwtEndUserPrincipal.from(jwt));
        Stream<User> filteredResult = fintFilterService.from(allUsers, filter);

        return Flux.fromStream(filteredResult);
    }

    @Autowired
    private UserRepository userRepository;
    @GetMapping("pageing")
    public ResponseEntity <Map<String,Object>> getUsersPagable(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "3") int size)
    {
        Pageable paging = PageRequest.of(page, size);
        Page<User> userPage;
        userPage = userRepository.findAllUsersPagable(paging);
        List<User> content = userPage.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("totalItems", userPage.getTotalElements());
        response.put("users", content);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //TODO: erstattes av getUserbyfilter når fint-antlr støtter startsWith()
    @GetMapping("/startswith")
    public Flux<User> getAllUsersFirstnameStartingWith(@RequestParam("firstnamepart") String firstnamepart,
                                                       @AuthenticationPrincipal Jwt jwt){
        log.info("Search for users with firstname startng with : " + firstnamepart);
        return userService.getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal.from(jwt),firstnamepart);

    }
}
