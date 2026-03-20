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

    public void save(String key, FactoryUser user) {
        if(user == null)
        {
            log.info("Received tombstone for user: {}", key);
            markUserDeleted(key);
            return;
        }
        log.info("Received user with resourceId: {}", user.resourceId());
        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.resourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));
    }

    public void markUserDeleted(String key) {
        userRepository.findUserByResourceIdEqualsIgnoreCase(key).ifPresent(user -> {
            user.setStatus(UserStatus.DELETED);
            user.setStatusChanged(Date.from(Instant.now()));
            userRepository.save(user);
            userEntityProducerService.publish(user);
        });
    }

    private Runnable onSaveNewUser(FactoryUser user) {
        return () -> {
            if (UserStatus.VALID_STATUSES.contains(user.fintStatus())) {
                User newUser = fromFactoryUser(user)
                        .status(getUserStatus(user))
                        .statusChanged(Date.from(Instant.now())).build();
                userRepository.save(newUser);
                log.info("Create new user: {}, with IdentityProviderUserObjectId: {}", newUser.getId(), newUser.getIdentityProviderUserObjectId());
                userEntityProducerService.publish(newUser);
            }

        };
    }

    private Consumer<User> onSaveExistingUser(FactoryUser incomingUser) {
        return existingUser -> {
           existingUser = mapFromIncomingUser(existingUser, incomingUser);
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


    public User mapFromIncomingUser(User existing, FactoryUser incoming) {
        String newStatus = getUserStatus(incoming);
        Date statusChanged = !Objects.equals(newStatus, existing.getStatus()) ? Date.from(Instant.now()) : existing.getStatusChanged();
        if(!UserStatus.VALID_STATUSES.contains(newStatus)) {
            existing.setStatus(UserStatus.INVALID);
            existing.setStatusChanged(statusChanged);
            return existing;
        }

        return fromFactoryUser(incoming)
                .status(newStatus)
                .organisationUnitIds(incoming.organisationUnitIds())
                .id(existing.getId())
                .statusChanged(statusChanged)
                .build();
    }

    public List<User> deactivateOldUsers() {
        Instant now = Instant.now();
        List<User> outdatedUsers = userRepository.findAll().stream().filter(
                user -> isOutdated(user, now)).toList();

        outdatedUsers.forEach(user -> {
            user.setStatus(UserStatus.DISABLED);
            user.setStatusChanged(Date.from(now));
            log.info("User with id: {} was valid until {} and will be deactivated", user.getId(), user.getValidTo());
        });
        userRepository.saveAll(outdatedUsers);
        return outdatedUsers;
    }

    private boolean isOutdated(User user, Instant now) {
        return user.getValidTo() != null && user.getValidTo().toInstant().isBefore(now);
    }

    public User.UserBuilder fromFactoryUser(FactoryUser factoryUser) {
        return User.builder()
                .email(factoryUser.email())
                .userName(factoryUser.userName())
                .firstName(factoryUser.firstName())
                .lastName(factoryUser.lastName())
                .managerRef(factoryUser.managerRef())
                .userType(factoryUser.userType())
                .identityProviderUserObjectId(factoryUser.identityProviderUserObjectId())
                .resourceId(factoryUser.resourceId())
                .mainOrganisationUnitName(factoryUser.mainOrganisationUnitName())
                .mainOrganisationUnitId(factoryUser.mainOrganisationUnitId())
                .validFrom(factoryUser.validFrom())
                .validTo(factoryUser.validTo());
    }

    private String getUserStatus(FactoryUser factoryUser) {
        var entra = factoryUser.entraStatus();
        var fint  = factoryUser.fintStatus();

        if (UserStatus.DELETED.equals(entra)) return UserStatus.DELETED;
        if (UserStatus.INVALID.equals(fint))  return UserStatus.INVALID;

        if (UserStatus.ACTIVE.equals(entra) && UserStatus.ACTIVE.equals(fint)) {
            var now = new Date();
            return (factoryUser.validFrom() == null || !factoryUser.validFrom().after(now)) &&
                    (factoryUser.validTo()   == null || !factoryUser.validTo().before(now))
                    ? UserStatus.ACTIVE
                    : UserStatus.DISABLED;
        }

        return UserStatus.DISABLED;
    }
}
