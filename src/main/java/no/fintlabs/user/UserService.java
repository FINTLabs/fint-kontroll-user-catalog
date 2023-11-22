package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.MemberService;
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

    private final MemberService memberService;
    private final AuthorizationClient authorizationClient;


    public UserService(UserRepository userRepository, UserEntityProducerService userEntityProducerService, MemberService memberService, AuthorizationClient authorizationClient) {
        this.userRepository = userRepository;
        this.userEntityProducerService = userEntityProducerService;
        this.memberService = memberService;
        this.authorizationClient = authorizationClient;
    }

    public void save(User user) {

        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.getResourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));


    }

    private Runnable onSaveNewUser(User user) {
        return () -> {
            User newUser = userRepository.save(user);
            log.info("Create new user: " + user.getId());
            memberService.process(memberService.create(newUser));
            log.info("created kontrollUser: " + newUser.getIdentityProviderUserObjectId());
            userEntityProducerService.publish(newUser);
        };
    }

    private Consumer<User> onSaveExistingUser(User user) {
        return existingUser -> {
            user.setId(existingUser.getId());
            log.info("Update user: " + user.getId());
            memberService.process(memberService.create(user));
            User savedUser = userRepository.save(user);
            log.info("update kontrollUser: " + savedUser.getIdentityProviderUserObjectId());
            userEntityProducerService.publish(savedUser);
        };
    }

    public DetailedUser getDetailedUserById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> allAuthorizedOrgIDs = getAllAutorizedOrgUnitIDs();

        User requestedUser = getUserById(id).orElse(new User());
        String requestedUserOrgID = requestedUser.getMainOrganisationUnitId();

        boolean requestedOrgIDInScope = allAuthorizedOrgIDs.contains(requestedUserOrgID);

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
        return
                userRepository.findById(id);
    }


    public List<SimpleUser> getSimpleUsers(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            String userType) {

        List<User> users;


        if ((orgUnits == null) && !(userType.equals("ALLTYPES"))) {
            users = userRepository.findUsersByNameType(search, userType);
            return users
                    .stream()
                    .map(User::toSimpleUser)
                    .toList();
        }

        if ((orgUnits != null) && (userType.equals("ALLTYPES"))) {
            users = userRepository.findUsersByNameOrg(search, orgUnits);
            return users
                    .stream()
                    .map(User::toSimpleUser)
                    .toList();
        }

        if ((orgUnits == null) && (userType.equals("ALLTYPES"))) {
            users = userRepository.findUsersByName(search);
            return users
                    .stream()
                    .map(User::toSimpleUser)
                    .toList();
        }


        users = userRepository.findUsersByNameOrgType(search, orgUnits, userType);
        return users
                .stream()
                .map(User::toSimpleUser)
                .toList();
    }


    public List<String> getAllAutorizedOrgUnitIDs() {

        List<Scope> scope = authorizationClient.getUserScopes();
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

        return getAllAutorizedOrgUnitIDs().stream()
                .filter(requestedOgUnits::contains)
                .toList();
    }
}
