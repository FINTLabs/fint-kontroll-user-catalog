package no.fintlabs.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.dto.UserDTOforDetails;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Value("${fint.kontroll.user-catalog.pagesize}")
    int pagesize;


    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsersPagable(@AuthenticationPrincipal Jwt jwt,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "1") int size) {
        if (size == 1){ size = pagesize;};
        log.info("Finding " +size+ " of all users at page: " + page);
        return userService.getAllUserDTOsPagedAndSorted(FintJwtEndUserPrincipal.from(jwt), page, size);
    }

    @GetMapping({"/id/{id}"})
    public Mono<UserDTOforDetails> getUserDTOforDetailsById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        log.info("Fetchind DTO for user by id");
        return userService.getUserDTOforDetailsById(FintJwtEndUserPrincipal.from(jwt), id);
    }



    @GetMapping(params = "$filter")
    public Flux<User> getUsersByFilter(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(value = "$filter", required = false) String filter) {
        log.info("Seaching all users with filter : " + filter);
        Stream<User> allUsers = userService.getAllUsersStream(FintJwtEndUserPrincipal.from(jwt));
        Stream<User> filteredResult = fintFilterService.from(allUsers, filter);

        return Flux.fromStream(filteredResult);
    }

    @GetMapping({"/students"})
    public ResponseEntity<Map<String,Object>> getAllStudentsPaged(@AuthenticationPrincipal Jwt jwt,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "1") int size){
        if (size==1){size=pagesize;};
        log.info("Finding " +size+ " of all students at page: " + page);
        String userType = "STUDENT";
        return userService.getAllUserDTOsByTypePagedAndSorted(FintJwtEndUserPrincipal.from(jwt),page,size,userType);
    }

    @GetMapping({"/employees"})
    public ResponseEntity<Map<String,Object>> getAllEmployeesPaged(@AuthenticationPrincipal Jwt jwt,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam (defaultValue = "1") int size){
        if (size == 1){ size = pagesize;};
        log.info("Finding " +size+ " of all employees at page: " + page);
        String userType = "EMPLOYEE";
        return userService.getAllUserDTOsByTypePagedAndSorted(FintJwtEndUserPrincipal.from(jwt),page,size, userType);
    }



}
