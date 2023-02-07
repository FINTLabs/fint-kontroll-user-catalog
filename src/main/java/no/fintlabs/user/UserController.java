package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final ResponseFactory responseFactory;

    public UserController(UserService userService, ResponseFactory responseFactory) {
        this.userService = userService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(value = "$filter", required = false) String filter,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size) {

        log.info("Finding " + size + " of all users at page: " + page);

        return responseFactory.toResponseEntity(
                FintJwtEndUserPrincipal.from(jwt),
                filter, page, size);
    }

    @GetMapping({"{id}"})
    public Mono<DetailedUser> getUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id");
        return userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
    }
}
