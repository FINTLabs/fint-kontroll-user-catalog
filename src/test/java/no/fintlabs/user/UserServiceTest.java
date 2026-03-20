package no.fintlabs.user;

import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEntityProducerService userEntityProducerService;

    @Mock
    private AuthorizationClient authorizationClient;

    @InjectMocks
    private UserService userService;

    private Scope userScope;
    private Scope roleScope;

    @BeforeEach
    void setUp() {
        userScope = Scope.builder()
                .objectType("user")
                .orgUnits(List.of("198", "2", "3"))
                .build();

        roleScope = Scope.builder()
                .objectType("role")
                .orgUnits(List.of("999"))
                .build();
    }

    @Test
    void shouldGetAllAuthorizedOrgUnitIdsForUserScopesOnly() {
        when(authorizationClient.getUserScopesList()).thenReturn(List.of(userScope, roleScope));

        List<String> authorizedOrgIds = userService.getAllAutorizedOrgUnitIDs();

        assertEquals(List.of("198", "2", "3"), authorizedOrgIds);
        verify(authorizationClient).getUserScopesList();
    }

    @Test
    void shouldCompareRequestedOrgUnitIdsWithOpa() {
        when(authorizationClient.getUserScopesList()).thenReturn(List.of(userScope, roleScope));

        List<String> result = userService.compareRequestedOrgUnitIDsWithOPA(List.of("198", "2", "5"));

        assertEquals(List.of("198", "2"), result);
    }

    @Test
    void shouldReturnRequestedOrgUnitsWhenOpaHasAllOrgUnits() {
        Scope allOrgUnitsScope = Scope.builder()
                .objectType("user")
                .orgUnits(List.of(OrgUnitType.ALLORGUNITS.name()))
                .build();

        when(authorizationClient.getUserScopesList()).thenReturn(List.of(allOrgUnitsScope));

        List<String> result = userService.compareRequestedOrgUnitIDsWithOPA(List.of("198", "2", "5"));

        assertEquals(List.of("198", "2", "5"), result);
    }

    @Test
    void shouldPermitAccessToDetailedUserWhenOrgUnitIsInScope() {
        User requestedUser = User.builder()
                .id(1L)
                .userType("STUDENT")
                .userName("titten@tei.no")
                .email("titten@tei.no")
                .firstName("Titten")
                .lastName("Tei")
                .mainOrganisationUnitName("Sjokodorisei Corp")
                .mainOrganisationUnitId("198")
                .resourceId("4711")
                .build();

        FintJwtEndUserPrincipal principal = new FintJwtEndUserPrincipal();
        principal.setMail("titten@tei.no");

        when(authorizationClient.getUserScopesList()).thenReturn(List.of(userScope));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestedUser));

        DetailedUser foundDetailedUser = userService.getDetailedUserById(principal, 1L);

        assertEquals(requestedUser.getId(), foundDetailedUser.getId());
        assertEquals(requestedUser.getUserName(), foundDetailedUser.getUserName());
    }

    @Test
    void shouldDenyAccessToDetailedUserWhenOrgUnitIsNotInScope() {
        FintJwtEndUserPrincipal principal = new FintJwtEndUserPrincipal();
        principal.setMail("titten@tei.no");

        when(authorizationClient.getUserScopesList()).thenReturn(List.of());

        DetailedUser foundDetailedUser = userService.getDetailedUserById(principal, 1L);

        assertEquals(new DetailedUser(), foundDetailedUser);
    }

    @Test
    void shouldReturnOnlyUsersWithIdentityProviderUserObjectId() {
        User user1 = User.builder()
                .id(1L)
                .identityProviderUserObjectId(UUID.fromString("3f2b9b63-47e9-43c2-9d61-dd078d621479"))
                .build();

        User user2 = User.builder()
                .id(2L)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user1, result.getFirst());
    }

    @Test
    void shouldCreateAndPublishNewUserWhenIncomingUserIsValidAndNotFound() {
        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .firstName("Titten")
                .lastName("Tei")
                .userName("titten@tei.no")
                .email("titten@tei.no")
                .identityProviderUserObjectId(UUID.fromString("3f2b9b63-47e9-43c2-9d61-dd078d621479"))
                .mainOrganisationUnitId("198")
                .mainOrganisationUnitName("Org")
                .organisationUnitIds(Set.of("198"))
                .userType("EXTERNAL")
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        when(userRepository.findUserByResourceIdEqualsIgnoreCase("4711")).thenReturn(Optional.empty());

        userService.save("4711", incomingUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userEntityProducerService).publish(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertEquals("4711", savedUser.getResourceId());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertNotNull(savedUser.getStatusChanged());
    }

    @Test
    void shouldUseOnSaveExistingUserPathWhenUserAlreadyExists() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .statusChanged(originalStatusChanged)
                .firstName("Old")
                .lastName("Name")
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .firstName("Updated")
                .lastName("User")
                .userName("updated@example.com")
                .email("updated@example.com")
                .userType("EXTERNAL")
                .mainOrganisationUnitId("198")
                .mainOrganisationUnitName("Org")
                .organisationUnitIds(Set.of("198", "200"))
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        when(userRepository.findUserByResourceIdEqualsIgnoreCase("4711")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.save("4711", incomingUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userEntityProducerService).publish(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertEquals(1L, savedUser.getId());
        assertEquals("4711", savedUser.getResourceId());
        assertEquals("Updated", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals("updated@example.com", savedUser.getUserName());
        assertEquals("updated@example.com", savedUser.getEmail());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertEquals(originalStatusChanged, savedUser.getStatusChanged());
        assertEquals(Set.of("198", "200"), savedUser.getOrganisationUnitIds());
    }

    @Test
    void shouldNotCreateUserWhenIncomingUserStatusIsInvalid() {
        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .fintStatus(UserStatus.INVALID)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        when(userRepository.findUserByResourceIdEqualsIgnoreCase("4711")).thenReturn(Optional.empty());

        userService.save("4711", incomingUser);

        verify(userRepository, never()).save(any(User.class));
        verify(userEntityProducerService, never()).publish(any(User.class));
    }

    @Test
    void shouldMarkUserDeletedWhenSaveReceivesTombstone() {
        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findUserByResourceIdEqualsIgnoreCase("4711")).thenReturn(Optional.of(existingUser));

        userService.save("4711", null);

        assertEquals(UserStatus.DELETED, existingUser.getStatus());
        assertNotNull(existingUser.getStatusChanged());
        verify(userRepository).save(existingUser);
        verify(userEntityProducerService).publish(existingUser);
    }

    @Test
    void shouldMarkUserDeletedWhenUserExists() {
        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findUserByResourceIdEqualsIgnoreCase("4711")).thenReturn(Optional.of(existingUser));

        userService.markUserDeleted("4711");

        assertEquals(UserStatus.DELETED, existingUser.getStatus());
        assertNotNull(existingUser.getStatusChanged());
        verify(userRepository).save(existingUser);
        verify(userEntityProducerService).publish(existingUser);
    }

    @Test
    void shouldMapExistingUserToInvalidWhenIncomingStatusIsInvalid() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .statusChanged(originalStatusChanged)
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .fintStatus(UserStatus.INVALID)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        User result = userService.mapFromIncomingUser(existingUser, incomingUser);

        assertEquals(UserStatus.INVALID, result.getStatus());
        assertNotNull(result.getStatusChanged());
    }

    @Test
    void shouldMapExistingUserToActiveAndKeepStatusChangedWhenStatusDoesNotChange() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .statusChanged(originalStatusChanged)
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .firstName("Updated")
                .lastName("User")
                .userName("updated@example.com")
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .organisationUnitIds(Set.of("198", "200"))
                .build();

        User result = userService.mapFromIncomingUser(existingUser, incomingUser);

        assertEquals(1L, result.getId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertEquals(originalStatusChanged, result.getStatusChanged());
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("updated@example.com", result.getUserName());
        assertEquals(Set.of("198", "200"), result.getOrganisationUnitIds());
    }

    @Test
    void shouldMapExistingUserToDisabledAndUpdateStatusChangedWhenStatusChanges() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .statusChanged(originalStatusChanged)
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.DISABLED)
                .build();

        User result = userService.mapFromIncomingUser(existingUser, incomingUser);

        assertEquals(UserStatus.DISABLED, result.getStatus());
        assertNotNull(result.getStatusChanged());
        assertTrue(result.getStatusChanged().after(originalStatusChanged));
    }

    @Test
    void shouldMutateExistingUserAndSetInvalidWhenIncomingStatusResolvesToDeleted() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.ACTIVE)
                .statusChanged(originalStatusChanged)
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.DELETED)
                .build();

        User result = userService.mapFromIncomingUser(existingUser, incomingUser);

        assertSame(existingUser, result);
        assertEquals(UserStatus.INVALID, result.getStatus());
        assertNotNull(result.getStatusChanged());
        assertTrue(result.getStatusChanged().after(originalStatusChanged));
    }

    @Test
    void shouldKeepStatusChangedWhenInvalidStatusMatchesExistingStatus() {
        Date originalStatusChanged = Date.from(Instant.parse("2024-01-01T00:00:00Z"));

        User existingUser = User.builder()
                .id(1L)
                .resourceId("4711")
                .status(UserStatus.INVALID)
                .statusChanged(originalStatusChanged)
                .build();

        FactoryUser incomingUser = FactoryUser.builder()
                .resourceId("4711")
                .fintStatus(UserStatus.INVALID)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        User result = userService.mapFromIncomingUser(existingUser, incomingUser);

        assertSame(existingUser, result);
        assertEquals(UserStatus.INVALID, result.getStatus());
        assertEquals(originalStatusChanged, result.getStatusChanged());
    }

    @Test
    void shouldReturnDeletedWhenEntraStatusIsDeleted() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.DELETED)
                .build();

        assertEquals(UserStatus.DELETED, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnInvalidWhenFintStatusIsInvalid() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.INVALID)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        assertEquals(UserStatus.INVALID, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnActiveWhenStatusesAreActiveAndNoDateRestrictions() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        assertEquals(UserStatus.ACTIVE, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnActiveWhenStatusesAreActiveAndCurrentDateIsWithinWindow() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .validFrom(Date.from(Instant.now().minusSeconds(60)))
                .validTo(Date.from(Instant.now().plusSeconds(60)))
                .build();

        assertEquals(UserStatus.ACTIVE, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnDisabledWhenStatusesAreActiveButValidFromIsInFuture() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .validFrom(Date.from(Instant.now().plusSeconds(60)))
                .build();

        assertEquals(UserStatus.DISABLED, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnDisabledWhenStatusesAreActiveButValidToIsInPast() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.ACTIVE)
                .entraStatus(UserStatus.ACTIVE)
                .validTo(Date.from(Instant.now().minusSeconds(60)))
                .build();

        assertEquals(UserStatus.DISABLED, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldReturnDisabledWhenStatusesAreNotDeletedInvalidOrBothActive() throws Exception {
        FactoryUser incomingUser = FactoryUser.builder()
                .fintStatus(UserStatus.DISABLED)
                .entraStatus(UserStatus.ACTIVE)
                .build();

        assertEquals(UserStatus.DISABLED, invokeGetUserStatus(incomingUser));
    }

    @Test
    void shouldDeactivateOutdatedUsers() {
        Instant now = Instant.now();

        User outdatedUser = User.builder()
                .id(1L)
                .status(UserStatus.ACTIVE)
                .validTo(Date.from(now.minusSeconds(60)))
                .build();

        User currentUser = User.builder()
                .id(2L)
                .status(UserStatus.ACTIVE)
                .validTo(Date.from(now.plusSeconds(60)))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(outdatedUser, currentUser));

        List<User> result = userService.deactivateOldUsers();

        assertEquals(1, result.size());
        assertEquals(outdatedUser, result.getFirst());
        assertEquals(UserStatus.DISABLED, outdatedUser.getStatus());
        assertNotNull(outdatedUser.getStatusChanged());
        verify(userRepository).saveAll(List.of(outdatedUser));
    }

    @Test
    void shouldReturnOnlyActiveUsersAsSimpleUsersFromSpecification() {
        User activeUser1 = User.builder()
                .id(1L)
                .firstName("Active")
                .lastName("One")
                .status(UserStatus.ACTIVE)
                .build();

        User inactiveUser = User.builder()
                .id(2L)
                .firstName("Inactive")
                .lastName("User")
                .status(UserStatus.DISABLED)
                .build();

        User activeUser2 = User.builder()
                .id(3L)
                .firstName("Active")
                .lastName("Two")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(activeUser1, inactiveUser, activeUser2));

        List<SimpleUser> result = userService.getSimpleUsersUsingSpec("Test", List.of("ORG1"), List.of("TYPE1"));

        assertEquals(2, result.size());
        assertTrue(result.contains(activeUser1.toSimpleUser()));
        assertTrue(result.contains(activeUser2.toSimpleUser()));
    }

    private String invokeGetUserStatus(FactoryUser factoryUser) throws Exception {
        Method method = UserService.class.getDeclaredMethod("getUserStatus", FactoryUser.class);
        method.setAccessible(true);
        return (String) method.invoke(userService, factoryUser);
    }
}