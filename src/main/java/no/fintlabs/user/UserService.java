package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static no.fintlabs.user.UserStatus.INVALID;


@Service
@Slf4j
@Transactional
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
        log.info("Received user with resourceId: {}", user.getResourceId());
        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.getResourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));
    }

    private Runnable onSaveNewUser(User user) {
        return () -> {
            if (!UserStatus.INVALID.equals(user.getStatus())) {
                User saved = userRepository.save(user);
                log.info("Create new user: {}, with IdentityProviderUserObjectId:", saved.getId(), saved.getIdentityProviderUserObjectId());
                userEntityProducerService.publish(saved);
            }

        };
    }

    private Consumer<User> onSaveExistingUser(User incomingUser) {
        return existingUser -> {
            mapFromIncomingUser(existingUser, incomingUser);
            log.debug("Update user: {}", existingUser.getId());
            User savedUser = userRepository.save(existingUser);
            userEntityProducerService.publish(savedUser);
        };
    }

    public DetailedUser getDetailedUserById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> allAuthorizedOrgIDs = getAllAutorizedOrgUnitIDs();

        User requestedUser = getUserById(id).orElse(new User());
        String requestedUserOrgID = requestedUser.getMainOrganisationUnitId();

        boolean requestedOrgIDInScope = allAuthorizedOrgIDs.contains(requestedUserOrgID)
                || allAuthorizedOrgIDs.contains(OrgUnitType.ALLORGUNITS.name());

        if (requestedOrgIDInScope) {
            DetailedUser requestedDetailedUser = requestedUser.toDetailedUser();
            log.info("User " + principal.getMail() + " has access to users in orgID: " + requestedUserOrgID);
            return requestedDetailedUser;
        } else {
            log.info("User " + principal.getMail() + " are not granted access to users in orgID: " + requestedUserOrgID);
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
    ) {
        UserSpecificationBuilder userSpesification = new UserSpecificationBuilder(search, orgUnits, userType);
        List<User> userList = userRepository.findAll(userSpesification.build());

        return userList.stream()
                .filter(user -> user.getStatus().equals(UserStatus.ACTIVE))
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

        if (orgUnitsfromOPA.contains(OrgUnitType.ALLORGUNITS.name())) {
            return requestedOgUnits;
        }

        return orgUnitsfromOPA.stream()
                .filter(requestedOgUnits::contains)
                .toList();
    }


    public static void mapFromIncomingUser(User existing, User incoming) {
        if (existing == null || incoming == null) return;

        if(INVALID.equals(incoming.getStatus())) {
            existing.setStatus(INVALID);
            return;
        }
        existing.setResourceId(incoming.getResourceId());
        existing.setFirstName(incoming.getFirstName());
        existing.setLastName(incoming.getLastName());
        existing.setUserType(incoming.getUserType());
        existing.setUserName(incoming.getUserName());
        existing.setIdentityProviderUserObjectId(incoming.getIdentityProviderUserObjectId());
        existing.setMainOrganisationUnitName(incoming.getMainOrganisationUnitName());
        existing.setMainOrganisationUnitId(incoming.getMainOrganisationUnitId());
        existing.setOrganisationUnitIds(incoming.getOrganisationUnitIds() == null
                ? new ArrayList<>()
                : new ArrayList<>(incoming.getOrganisationUnitIds()));
        existing.setEmail(incoming.getEmail());
        existing.setManagerRef(incoming.getManagerRef());
        existing.setStatus(incoming.getStatus());
        existing.setStatusChanged(incoming.getStatusChanged());
        existing.setValidFrom(incoming.getValidFrom());
        existing.setValidTo(incoming.getValidTo());
    }

    public List<User> deactivateOldUsers() {
        Instant now = Instant.now();
        List<User> outdatedUsers = userRepository.findAll().stream().filter(
                user -> isOutdated(user, now)).toList();

        outdatedUsers.forEach(user -> {
            user.setStatus(UserStatus.DISABLED);
            log.info("User with id: {} was valid until {} and will be deactivated", user.getId(), user.getValidTo());
        });
        userRepository.saveAll(outdatedUsers);
        return outdatedUsers;
    }

    private boolean isOutdated(User user, Instant now) {
        return user.getValidTo() != null && user.getValidTo().toInstant().isBefore(now);
    }
}
