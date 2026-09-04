package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${fint.kontroll.user.days-before-start-employee:0}")
    private int daysBeforeStartEmployee;

    @Value("${fint.kontroll.user.days-before-start-student:0}")
    private int daysBeforeStartStudent;


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
            user.setEntraStatus(UserStatus.DELETED);
            user.setStatusChanged(Date.from(Instant.now()));
            userRepository.save(user);
            userEntityProducerService.publish(user);
        });
    }

    private Runnable onSaveNewUser(FactoryUser user) {
        return () -> {
            String status = getUserStatus(user);
            if (shouldCreateUser(status)) {
                User newUser = fromFactoryUser(user);
                newUser.setStatus(status);
                newUser.setStatusChanged(Date.from(Instant.now()));
                userRepository.save(newUser);
                log.info("Create new user: {}, with IdentityProviderUserObjectId: {}", newUser.getId(), newUser.getIdentityProviderUserObjectId());
                userEntityProducerService.publish(newUser);
            }

        };
    }

    private Consumer<User> onSaveExistingUser(FactoryUser incomingUser) {
        return existingUser -> {
          User mappedIncoming = mapFromIncomingUser(existingUser, incomingUser);
            log.info("Update existing user: {}", existingUser.getId());
            if(!mappedIncoming.equals(existingUser))
            {
                User savedUser = userRepository.save(mappedIncoming);
                userEntityProducerService.publish(savedUser);
            }
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
                .filter(user -> UserStatus.ACTIVE.equals(user.getStatus()))
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
        if(shouldUpdateStatusOnly(newStatus)) {
            // Invalid/deleted source updates can be minimal. Preserve the last
            // complete user details and only change catalog status metadata.
            return existing.toBuilder()
                    .fintStatus(incoming.fintStatus())
                    .entraStatus(incoming.entraStatus())
                    .status(newStatus)
                    .statusChanged(statusChanged)
                    .build();
        }

        User mappedUser = fromFactoryUser(incoming);
        mappedUser.setStatus(newStatus);
        mappedUser.setOrganisationUnitIds(incoming.organisationUnitIds());
        mappedUser.setId(existing.getId());
        mappedUser.setStatusChanged(statusChanged);
        return mappedUser;
    }

    public List<User> deactivateOldUsers() {
        Instant now = Instant.now();
        List<User> outdatedUsers = userRepository.findAll().stream()
                .filter(user -> UserStatus.ACTIVE.equals(user.getStatus()))
                .filter(user -> isOutdated(user, now))
                .toList();

        outdatedUsers.forEach(user -> {
            user.setStatus(UserStatus.DISABLED);
            user.setStatusChanged(Date.from(now));
            log.info("User with id: {} was valid until {} and will be deactivated", user.getId(), user.getValidTo());
        });
        userRepository.saveAll(outdatedUsers);
        return outdatedUsers;
    }

    public List<User> reconcileTimeBasedUserStatuses() {
        Date statusChanged = Date.from(Instant.now());
        List<User> changedUsers = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            String reconciledStatus = getUserStatus(user);
            if (!Objects.equals(reconciledStatus, user.getStatus())) {
                user.setStatus(reconciledStatus);
                user.setStatusChanged(statusChanged);
                changedUsers.add(user);
            }
        }

        if (!changedUsers.isEmpty()) {
            userRepository.saveAll(changedUsers);
            userEntityProducerService.publishKontrollUsers("scheduled status reconciliation", changedUsers);
        }

        return changedUsers;
    }

    private boolean isOutdated(User user, Instant now) {
        return user.getValidTo() != null && user.getValidTo().toInstant().isBefore(now);
    }

    public User fromFactoryUser(FactoryUser factoryUser) {
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
                .validTo(factoryUser.validTo())
                .fintStatus(factoryUser.fintStatus())
                .entraStatus(factoryUser.entraStatus())
                .build();
    }

    private String getUserStatus(FactoryUser factoryUser) {
        return getUserStatus(
                factoryUser.entraStatus(),
                factoryUser.fintStatus(),
                factoryUser.validFrom(),
                factoryUser.validTo(),
                factoryUser.userType()
        );
    }

    private String getUserStatus(User user) {
        if (!UserStatus.VALID_STATUSES.contains(user.getStatus())) {
            return user.getStatus();
        }

        if (UserStatus.ACTIVE.equals(user.getStatus()) && isOutdated(user, Instant.now())) {
            return UserStatus.DISABLED;
        }

        if (user.getFintStatus() == null ||
                user.getEntraStatus() == null) {
            return user.getStatus();
        }

        return getUserStatus(
                user.getEntraStatus(),
                user.getFintStatus(),
                user.getValidFrom(),
                user.getValidTo(),
                user.getUserType()
        );
    }

    private String getUserStatus(String entra, String fint, Date validFrom, Date validTo, String userType) {
        // Factory publishes source facts only; final catalog status is derived here.
        if (UserStatus.DELETED.equals(entra)) return UserStatus.DELETED;

        Optional<String> legacyStatus = getStatusFromLegacyFintStatus(fint, entra, validFrom, validTo, userType);
        if (legacyStatus.isPresent()) return legacyStatus.get();

        if (FintStatus.INVALID.equals(fint))  return UserStatus.INVALID;
        if (!FintStatus.VALID.equals(fint)) return UserStatus.INVALID;

        return getStatusWhenFintDataIsValid(entra, validFrom, validTo, userType);
    }

    private Optional<String> getStatusFromLegacyFintStatus(
            String fintStatus,
            String entraStatus,
            Date validFrom,
            Date validTo,
            String userType
    ) {
        // TODO FKS-1648: Remove this rollout bridge after every factory emits
        // VALID/INVALID and legacy ACTIVE/DISABLED Kafka records can no longer
        // be replayed. Old factory versions put final catalog status in
        // fintStatus, so keep those values compatible during deployment.
        if (UserStatus.ACTIVE.equals(fintStatus)) {
            return Optional.of(getStatusWhenFintDataIsValid(entraStatus, validFrom, validTo, userType));
        }

        if (UserStatus.DISABLED.equals(fintStatus)) {
            return Optional.of(UserStatus.DISABLED);
        }

        return Optional.empty();
    }

    private String getStatusWhenFintDataIsValid(String entra, Date validFrom, Date validTo, String userType) {
        if (UserStatus.DISABLED.equals(entra)) return UserStatus.DISABLED;

        if (UserStatus.ACTIVE.equals(entra)) {
            var now = new Date();
            return (validFrom == null || !getValidFrom(validFrom, userType).after(now)) &&
                    (validTo   == null || !validTo.before(now))
                    ? UserStatus.ACTIVE
                    : UserStatus.DISABLED;
        }

        return UserStatus.DISABLED;
    }

    private boolean shouldCreateUser(String status) {
        return UserStatus.VALID_STATUSES.contains(status);
    }

    private boolean shouldUpdateStatusOnly(String status) {
        return UserStatus.INVALID.equals(status) || UserStatus.DELETED.equals(status);
    }

    private Date getValidFrom(Date validFrom, String userType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(validFrom);
        calendar.add(Calendar.DATE, -getDaysBeforeStart(userType));
        return calendar.getTime();
    }

    private int getDaysBeforeStart(String userType) {
        return "STUDENT".equals(userType) ? daysBeforeStartStudent : daysBeforeStartEmployee;
    }
}
