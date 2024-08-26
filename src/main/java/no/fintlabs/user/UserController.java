package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String,Object>> getSimpleUsers(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(value = "search",defaultValue = "%") String search,
                                                   @RequestParam(value = "orgUnits",required = false)List<String> orgUnits,
                                                   @RequestParam(value = "userType", defaultValue = "ALLTYPES") String userType,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size
                                                   ){

        log.info("Finding users with search: {} with orgUnitIDs: {} with UserType: {}", search, orgUnits, userType);

        if (orgUnits == null){
            List<String> allAuthorizedOrgUnitIDsFromOPA = userService.getAllAutorizedOrgUnitIDs();
            log.info("No orgUnits spesified. Returning users from all authorized orgUnits. Authorized orgUnitIDs: {}", allAuthorizedOrgUnitIDsFromOPA);
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search,allAuthorizedOrgUnitIDsFromOPA,userType,page,size);
        }
        else {
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search, userService.compareRequestedOrgUnitIDsWithOPA(orgUnits),userType,page,size);
        }
    }

    @GetMapping({"{id}"})
    public ResponseEntity<DetailedUser> getDetailedUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        DetailedUser detailedUserById = userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
        if (detailedUserById.isValid()){
            return new ResponseEntity<>(detailedUserById, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);}
    }

    @GetMapping("/me")
    public ResponseEntity<LoggedOnUser> getLoggedOnUser(@AuthenticationPrincipal Jwt jwt){
        FintJwtEndUserPrincipal principal = FintJwtEndUserPrincipal.from(jwt);
        LoggedOnUser loggedOnUser = new LoggedOnUser(
                principal.getGivenName(),
                principal.getSurname(),
                principal.getOrgId(),
                principal.getMail()
        );

        return new ResponseEntity<>(loggedOnUser,HttpStatus.OK);
    }

}
