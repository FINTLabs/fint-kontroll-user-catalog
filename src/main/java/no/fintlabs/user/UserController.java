package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final ResponseFactory responseFactory;

    public UserController(UserService userService, ResponseFactory responseFactory, AuthorizationClient authorizationClient) {
        this.userService = userService;
        this.responseFactory = responseFactory;
    }


    @GetMapping()
    public ResponseEntity<Map<String,Object>> getUsers(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(value = "search",defaultValue = "%") String search,
                                                   @RequestParam(value = "orgUnits",required = false)List<String> orgUnits,
                                                   @RequestParam(value = "userType", defaultValue = "ALLTYPES") String userType,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size
                                                   ){

        log.info("Finding users with search: " + search + " with orgUnitIDs: " + orgUnits + " with UserType: " + userType);
        List<String> allAuthorizedOrgUnitIDsFromOPA = userService.getAllAutorizedOrgUnitIDs();

        if (orgUnits == null){
            log.info("No orgUnits spesified. Returning users from all authorized orgUnits. Authorized orgUnitIDs: " + allAuthorizedOrgUnitIDsFromOPA);
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search,allAuthorizedOrgUnitIDsFromOPA,userType,page,size);
        }
        else {
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search, userService.compareRequestedOrgUnitIDsWithOPA(orgUnits),userType,page,size);
        }
    }

    @GetMapping({"{id}"})
    public DetailedUser getUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id: " + id);
        return userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
    }
}
