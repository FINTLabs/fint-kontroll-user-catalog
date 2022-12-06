package no.fintlabs.controller;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.User;
import no.fintlabs.user.UserService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> getAllUsers(@AuthenticationPrincipal Jwt jwt){
        log.info("Fetching all users");
        return userService.getAllUsers(FintJwtEndUserPrincipal.from(jwt));
    }


    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id, @AuthenticationPrincipal Jwt jwt){
        log.info("Fetching user info for : "+ id);
        return  userService.getById(FintJwtEndUserPrincipal.from(jwt), id);
    }

    @GetMapping("/usertype")
    public Flux<User> getAllUsersByUserType(@RequestParam("usertype") String usertype, @AuthenticationPrincipal Jwt jwt){
        log.info("userType :: " + usertype);
        usertype = usertype.toUpperCase();

        return userService.getAllUsersByUserType(FintJwtEndUserPrincipal.from(jwt), usertype);
    }

    @GetMapping("/firstname")
    public Flux<User> getAllUsersFirstnameStartingWith(@RequestParam("firstnamepart") String firstnamepart,
    @AuthenticationPrincipal Jwt jwt){
        log.info("Search for users with firstname startng with : " + firstnamepart);
        return userService.getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal.from(jwt),firstnamepart);

    }


    
    
    @GetMapping("/tull")
    public ResponseEntity<String> tull(){
        log.info("her da...?");
        return ResponseEntity.ok("tull");
    }
}
