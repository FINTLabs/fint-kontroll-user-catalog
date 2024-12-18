package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.AuthRole;
import no.fintlabs.opa.model.MenuItem;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserEntityProducerService userEntityProducerService;

    private final ResponseFactory responseFactory;
    private final AuthorizationClient authorizationClient;

    public UserController(UserService userService, ResponseFactory responseFactory, AuthorizationClient authorizationClient, UserEntityProducerService userEntityProducerService) {
        this.userService = userService;
        this.responseFactory = responseFactory;
        this.authorizationClient = authorizationClient;
        this.userEntityProducerService = userEntityProducerService;
    }


    @GetMapping()
    public ResponseEntity<Map<String, Object>> getSimpleUsers(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestParam(value = "search", defaultValue = "%") String search,
                                                              @RequestParam(value = "orgUnits", required = false) List<String> orgUnits,
                                                              @RequestParam(value = "userType", defaultValue = "ALLTYPES") String userType,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "${fint.kontroll.user-catalog.pagesize:20}") int size
    ) {

        log.info("Finding users with search: {} with orgUnitIDs: {} with UserType: {}", search, orgUnits, userType);

        if (orgUnits == null) {
            List<String> allAuthorizedOrgUnitIDsFromOPA = userService.getAllAutorizedOrgUnitIDs();
            log.info("No orgUnits spesified. Returning users from all authorized orgUnits. Authorized orgUnitIDs: {}", allAuthorizedOrgUnitIDsFromOPA);
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt), search, allAuthorizedOrgUnitIDsFromOPA, userType, page, size);
        } else {
            return responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt), search, userService.compareRequestedOrgUnitIDsWithOPA(orgUnits), userType, page, size);
        }
    }

    @GetMapping({"{id}"})
    public ResponseEntity<DetailedUser> getDetailedUserById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        DetailedUser detailedUserById = userService.getDetailedUserById(FintJwtEndUserPrincipal.from(jwt), id);
        if (detailedUserById.isValid()) {
            return new ResponseEntity<>(detailedUserById, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LoggedOnUser> getLoggedOnUser(@AuthenticationPrincipal Jwt jwt) {
        FintJwtEndUserPrincipal principal = FintJwtEndUserPrincipal.from(jwt);

        List<AuthRole> userRoles = authorizationClient.getUserRoles();
        List<MenuItem> menuItems = authorizationClient.getMenuItems();

        LoggedOnUser loggedOnUser = new LoggedOnUser(
                principal.getGivenName(),
                principal.getSurname(),
                principal.getOrgId(),
                principal.getMail(),
                userRoles,
                menuItems
        );

        return new ResponseEntity<>(loggedOnUser, HttpStatus.OK);
    }

    @PostMapping("/republish")
    public ResponseEntity<Map<String,Object>> republishKontrollUsers(@AuthenticationPrincipal Jwt jwt) {
        if (!authorizationClient.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have access to republish all kontrollusers");
        }

        String triggerType = "admin (" + FintJwtEndUserPrincipal.from(jwt).getMail() + ") request";
        List<User> allUsers = userService.getAllUsers();

        if (allUsers.isEmpty()) {
            log.info("No users found to publish");
            return responseFactory.toResponseEntity("No users found to publish");
        }
        int noOfPublishedUsers = userEntityProducerService.publishAllKontrollUsers(triggerType, allUsers);

        return responseFactory.toResponseEntity("Republished " + noOfPublishedUsers + " users");
    }

    @PostMapping("/me/hasaccess")
    public ResponseEntity<List<AccessResponse>> hasAccess(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AccessRequest accessRequest) {

        FintJwtEndUserPrincipal principal = FintJwtEndUserPrincipal.from(jwt);

        if(accessRequest == null || accessRequest.getAccessRequests() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<AccessResponse> results = accessRequest.getAccessRequests().stream()
                .map(request -> {
                    boolean isAuthorized = authorizationClient.isAuthorized(
                            principal.getMail(),
                            request.getMethod(),
                            request.getUrl());
                    return new AccessResponse(request.getUrl(), isAuthorized);
                })
                .toList();

        return ResponseEntity.ok(results);
    }

}
