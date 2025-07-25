package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserEntityProducerService userEntityProducerService;
    private final AuthorizationClient authorizationClient;


    public UserService(UserRepository userRepository, UserEntityProducerService userEntityProducerService, AuthorizationClient authorizationClient) {
        this.userRepository = userRepository;
        this.userEntityProducerService = userEntityProducerService;
        this.authorizationClient = authorizationClient;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getIdentityProviderUserObjectId() != null)
                .toList();
    }

    public void save(User user) {
        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.getResourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));
    }

    private Runnable onSaveNewUser(User user) {
        return () -> {
            User newUser = userRepository.save(user);
            log.info("Create new user: {}", user.getId());
            log.info("Created kontrollUser: {}", newUser.getIdentityProviderUserObjectId());
            userEntityProducerService.publish(newUser);
        };
    }

    private Consumer<User> onSaveExistingUser(User user) {
        return existingUser -> {
            user.setId(existingUser.getId());
            log.debug("Update user: {}", user.getId());
            User savedUser = userRepository.save(user);
            log.debug("update kontrollUser: {}", savedUser.getIdentityProviderUserObjectId());
            userEntityProducerService.publish(savedUser);
        };
    }

    public DetailedUser getDetailedUserById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> allAuthorizedOrgIDs = getAllAutorizedOrgUnitIDs();

        User requestedUser = getUserById(id).orElse(new User());
        String requestedUserOrgID = requestedUser.getMainOrganisationUnitId();

        boolean requestedOrgIDInScope = allAuthorizedOrgIDs.contains(requestedUserOrgID )
                || allAuthorizedOrgIDs.contains(OrgUnitType.ALLORGUNITS.name());

        if (requestedOrgIDInScope){
            DetailedUser requestedDetailedUser = requestedUser.toDetailedUser();
            log.info("User "+principal.getMail()+" has access to users in orgID: " + requestedUserOrgID);
            return requestedDetailedUser;
        }
        else {
            log.info("User "+ principal.getMail() +" are not granted access to users in orgID: " + requestedUserOrgID);
            return new DetailedUser();
        }
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<SimpleUser> getSimpleUsersUsingSpec(
            String search,
            List<String> orgUnits,
            List<String> userType
    ){
        UserSpesificationBuilder userSpesification = new UserSpesificationBuilder(search,orgUnits,userType);
        List<User> userList = userRepository.findAll(userSpesification.build());

        return userList.stream()
                .filter(user -> user.getStatus().equals("ACTIVE"))
                .map(User::toSimpleUser)
                .toList();
    }


    public List<String> getAllAutorizedOrgUnitIDs() {

        List<Scope> scope = authorizationClient.getUserScopesList();

        List<String> authorizedOrgIDs = scope.stream()
                .filter(s -> s.getObjectType().equals("user"))
                .map(Scope::getOrgUnits)
                .flatMap(Collection::stream)
                .toList();
        log.info("UserScopes from OPA: " + scope);
        log.info("Authorized orgUnitIDs" + authorizedOrgIDs);

        return authorizedOrgIDs;
    }


    public List<String> compareRequestedOrgUnitIDsWithOPA(List<String> requestedOgUnits) {
        List<String> orgUnitsfromOPA = getAllAutorizedOrgUnitIDs();

        if (orgUnitsfromOPA.contains(OrgUnitType.ALLORGUNITS.name())){
            return requestedOgUnits;
        }

        return orgUnitsfromOPA.stream()
                .filter(requestedOgUnits::contains)
                .toList();
    }
}
