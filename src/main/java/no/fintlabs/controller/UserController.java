package no.fintlabs.controller;


import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.model.User;
import no.fintlabs.user.UserService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FintFilterService fintFilterService;

    public UserController(UserService userService, FintFilterService fintFilterService) {
        this.userService = userService;
        this.fintFilterService = fintFilterService;
    }

    @GetMapping
    public Flux<User> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        log.info("Fetching all users");

        return userService.getAllUsers(FintJwtEndUserPrincipal.from(jwt));
    }


    @GetMapping(params = "$filter")
    public Flux<User> getUsersByFilter(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(value = "$filter", required = false) String filter) {
        log.info("Seaching all users with filter : " + filter);
        Stream<User> allUsers = userService.getAllUsersStream(FintJwtEndUserPrincipal.from(jwt));
        Stream<User> filteredResult = fintFilterService.from(allUsers, filter);

        return Flux.fromStream(filteredResult);
    }


    @GetMapping("/pageing")
    public ResponseEntity<Map<String, Object>> getUsersPagable(@AuthenticationPrincipal Jwt jwt,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "3") int size) {
        log.info("Finding all users at page: " + page);
        return userService.getAllUsersPaged(FintJwtEndUserPrincipal.from(jwt), page, size);
    }




    @GetMapping("/allinone")
    public ResponseEntity<Map<String,Object>> getUsersAllinone(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "$filter",required = false) String filter,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size ){

    }




    //TODO: erstattes av getUserbyfilter når fint-antlr støtter startsWith()
    @GetMapping("/startswith")
    public Flux<User> getAllUsersFirstnameStartingWith(@RequestParam("firstnamepart") String firstnamepart,
                                                       @AuthenticationPrincipal Jwt jwt) {
        log.info("Search for users with firstname starting with : " + firstnamepart);
        return userService.getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal.from(jwt), firstnamepart);

    }


}
