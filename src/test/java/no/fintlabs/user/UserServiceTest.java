package no.fintlabs.user;

import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private AuthorizationClient authorizationClient;
    @BeforeEach
    public void init(){
        authorizationClient = mock(AuthorizationClient.class);
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository,  null, authorizationClient);
    }
    @Test
    public void testGetAllAutorizedOrgUnitIDs() {
        Scope scope1 = Scope.builder()
                .objectType("user")
                .orgUnits(List.of("198","2","3"))
                .build();
        Scope scope2 = Scope.builder()
                .objectType("role")
                .orgUnits(List.of("198","2","3"))
                .build();
        List<Scope> scopes= List.of(scope1,scope2);
        List<String> authorizedOrgIDs = List.of("198","2","3");

        when(authorizationClient.getUserScopesList()).thenReturn(scopes);

        List<String> foundOrgIDs= userService.getAllAutorizedOrgUnitIDs();

        assertEquals(authorizedOrgIDs,foundOrgIDs);

        verify(authorizationClient, times(1)).getUserScopesList();
    }

    @Test
    void testCompareRequestedOrgUnitIDsWithOPA() {

        Scope scope1 = Scope.builder()
                .objectType("user")
                .orgUnits(List.of("198","2","3"))
                .build();
        Scope scope2 = Scope.builder()
                .objectType("role")
                .orgUnits(List.of("198","2","3"))
                .build();
        List<Scope> scopes= List.of(scope1,scope2);
        List<String> requestedOrgIDs = List.of("198","2","5");
        List<String> authorizedOrgIDsForRequest = List.of("198","2");
        when(authorizationClient.getUserScopesList()).thenReturn(scopes);

        List<String> foundOrgIDs = userService.compareRequestedOrgUnitIDsWithOPA(requestedOrgIDs);

        assertEquals(authorizedOrgIDsForRequest,foundOrgIDs);
    }

    @Test
    void testGetDetailedUserById_shouldPermitAccess(){
        Scope scope1 = Scope.builder()
                .objectType("user")
                .orgUnits(List.of("198","2","3"))
                .build();
        Scope scope2 = Scope.builder()
                .objectType("role")
                .orgUnits(List.of("198","2","3"))
                .build();
        List<Scope> scopes= List.of(scope1,scope2);
        List<String> requestedOrgIDs = List.of("198","2","5");
        List<String> authorizedOrgIDsForRequest = List.of("198","2");
        User requestedUser = User.builder()
                .id(1L)
                .userType("STUDENT")
                .userName("titten@tei.no")
                .email("titten@tei.no")
                .firstName("Titten")
                .lastName("Tei")
                .mainOrganisationUnitName("Sjokodorisei Corp")
                .resourceId("4711")
                .mainOrganisationUnitId("198")
                .build();

        FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
        fintJwtEndUserPrincipal.setMail("titten@tei.no");
        when(authorizationClient.getUserScopesList()).thenReturn(scopes);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestedUser));
        DetailedUser requestedDetailedUser = requestedUser.toDetailedUser();

        DetailedUser foundDetailedUser = userService.getDetailedUserById(fintJwtEndUserPrincipal,1L);

        assertEquals(requestedDetailedUser.getId(), foundDetailedUser.getId());
    }
    @Test
    void testGetAllUsers_shouldReturnOnlyUsersWithNotNullIdpObjectId() {
        User user1 = User.builder()
                .id(1L)
                .identityProviderUserObjectId(UUID.fromString("3f2b9b63-47e9-43c2-9d61-dd078d621479"))
                .build();

        User user2 = User.builder()
                .id(2L)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1,user2));

        assertEquals(1, userService.getAllUsers().size());
        assertEquals(user1, userService.getAllUsers().get(0));
    }
}
