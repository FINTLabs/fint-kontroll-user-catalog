package no.fintlabs.user;

import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserService userService;
    private AuthorizationClient authorizationClient;
    @BeforeEach
    public void init(){
        authorizationClient = mock(AuthorizationClient.class);
        userService = new UserService(null, null, authorizationClient);
    }
    @Test
    public void testGetAllAutorizedOrgUnitIDs() {
        Scope scope1 = Scope.builder()
                .id("1")
                .objectType("user")
                .orgUnits(List.of("198","2","3"))
                .build();
        Scope scope2 = Scope.builder()
                .id("2")
                .objectType("role")
                .orgUnits(List.of("198","2","3"))
                .build();
        List<Scope> scopes= List.of(scope1,scope2);
        List<String> authorizedOrgIDs = List.of("198","2","3");

        when(authorizationClient.getUserScopes()).thenReturn(scopes);

        List<String> foundOrgIDs= userService.getAllAutorizedOrgUnitIDs();

        assertEquals(authorizedOrgIDs,foundOrgIDs);

        verify(authorizationClient, times(1)).getUserScopes();
    }

    @Test
    void testCompareRequestedOrgUnitIDsWithOPA() {

        Scope scope1 = Scope.builder()
                .id("1")
                .objectType("user")
                .orgUnits(List.of("198","2","3"))
                .build();
        Scope scope2 = Scope.builder()
                .id("2")
                .objectType("role")
                .orgUnits(List.of("198","2","3"))
                .build();
        List<Scope> scopes= List.of(scope1,scope2);
        List<String> requestedOrgIDs = List.of("198","2","5");
        List<String> authorizedOrgIDsForRequest = List.of("198","2");
        when(authorizationClient.getUserScopes()).thenReturn(scopes);

        List<String> foundOrgIDs = userService.compareRequestedOrgUnitIDsWithOPA(requestedOrgIDs);

        assertEquals(authorizedOrgIDsForRequest,foundOrgIDs);
    }
}