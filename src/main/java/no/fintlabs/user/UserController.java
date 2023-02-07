package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${fint.kontroll.user-catalog.pagesize}")
    int pageSize;

    .
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(value = "$filter", required = false) String filter,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize}") int size) {
//        if (size == 1) {
//            size = pageSize;
//        }

        log.info("Finding " + size + " of all users at page: " + page);

        return responseFactory.toResponseEntity(
                FintJwtEndUserPrincipal.from(jwt),
                filter, page, size);
        //return userService.getAllUserPagedAndSorted(FintJwtEndUserPrincipal.from(jwt), page, size);
    }

    @GetMapping({"{id}"})
    public Mono<DetailedUser> getUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id");
        return userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
    }


//    @GetMapping("/filter")
//    public ResponseEntity<Map<String, Object>> getUsersByFilter(@AuthenticationPrincipal Jwt jwt,
//                                                                @RequestParam(value = "$filter", required = false) String filter,
//                                                                @RequestParam(defaultValue = "0") int page,
//                                                                @RequestParam(defaultValue = "1") int size) {
//        if (size == 1) {
//            size = pageSize;
//        }
//        ;
//        log.info("Finding all users with filter : " + filter);
//
//        return responseFactory.toResponseEntity(
//                FintJwtEndUserPrincipal.from(jwt),
//                filter, page, size);
//    }

    @GetMapping({"/students"})
    public ResponseEntity<Map<String, Object>> getAllStudentsPaged(@AuthenticationPrincipal Jwt jwt,
                                                                   @RequestParam(value = "$filter", required = false) String filter,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "1") int size) {
        if (size == 1) {
            size = pageSize;
        }
        ;
        log.info("Finding " + size + " of all students at page: " + page);

        return responseFactory.toResponseEntity(
                FintJwtEndUserPrincipal.from(jwt),
                filter,
                "STUDENT",
                page,
                size
        );
        //String userType = "STUDENT";
        //return userService.getAllUserByTypePagedAndSorted(FintJwtEndUserPrincipal.from(jwt), page, size, userType);
    }

    @GetMapping({"/employees"})
    public ResponseEntity<Map<String, Object>> getAllEmployeesPaged(@AuthenticationPrincipal Jwt jwt,
                                                                    @RequestParam(value = "$filter", required = false) String filter,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "1") int size) {
        if (size == 1) {
            size = pageSize;
        }

        log.info("Finding " + size + " of all employees at page: " + page);
        return responseFactory.toResponseEntity(
                FintJwtEndUserPrincipal.from(jwt),
                filter,
                "EMPLOYEE",
                page,
                size
        );
//        String userType = "EMPLOYEE";
//        return userService.getAllUserByTypePagedAndSorted(FintJwtEndUserPrincipal.from(jwt), page, size, userType);
    }
}
