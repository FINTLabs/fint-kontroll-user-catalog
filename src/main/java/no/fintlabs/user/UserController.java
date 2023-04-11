package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    //@GetMapping(produces = APPLICATION_JSON_VALUE )
    public ResponseEntity<Map<String, Object>> getUsersUsingOdata(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(value = "$filter", required = false) String filter,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size) {

        log.info("Finding users with filter: " + filter + " at page: " + page + " (first page = 0)" );

        return responseFactory.toResponseEntity(
                FintJwtEndUserPrincipal.from(jwt),
                filter, page, size);
    }

    @GetMapping()
    public ResponseEntity<Map<String,Object>> getUsers(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(value = "search",defaultValue = "%") String search,
                                                   @RequestParam(value = "orgUnits",required = false)List<String> orgUnits,
                                                   @RequestParam(value = "userType", defaultValue = "ALLTYPES") String userType,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size
                                                   ){

        log.info("Finding users with search: " + search + " in orgunits: " + orgUnits + " with UserType: " + userType);
        return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search,orgUnits,userType,page,size);
    }

    @GetMapping({"{id}"})
    public Mono<DetailedUser> getUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id: " + id);
        return userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
    }
}
