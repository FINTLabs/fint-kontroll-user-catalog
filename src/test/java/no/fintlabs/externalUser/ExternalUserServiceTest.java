package no.fintlabs.externalUser;

import no.fintlabs.user.FactoryUser;
import no.fintlabs.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExternalUserServiceTest {

    @InjectMocks
    private ExternalUserService externalUserService;

    @Mock
    private UserService userService;

    @Test
    void shouldMarkUserDeletedWhenExternalUserIsNull() {
        externalUserService.convertAndSaveAsUser("4711", null);

        verify(userService).markUserDeleted("4711");
        verify(userService, never()).save(any(), any());
    }

    @Test
    void shouldConvertAndSaveExternalUser() {
        String key = "f37f3048-637a-11ee-8c99-0242ac120002";

        ExternalUserPayload payload = new ExternalUserPayload(
                "Titten",
                "Tei",
                "EXTERNAL",
                "titten@tei.no",
                "Org",
                "198",
                "titten@tei.no",
                "titten@tei.no",
                true
        );

        externalUserService.convertAndSaveAsUser(key, payload);

        ArgumentCaptor<FactoryUser> captor = ArgumentCaptor.forClass(FactoryUser.class);
        verify(userService).save(eq(key), captor.capture());

        FactoryUser saved = captor.getValue();
        assertEquals("Titten", saved.firstName());
        assertEquals("Tei (ekstern tei.no)", saved.lastName());
        assertEquals("EXTERNAL", saved.userType());
        assertEquals(key, saved.resourceId());
        assertEquals("titten@tei.no", saved.userName());
        assertEquals(UUID.fromString(key), saved.identityProviderUserObjectId());
        assertEquals("Org", saved.mainOrganisationUnitName());
        assertEquals("198", saved.mainOrganisationUnitId());
        assertEquals("titten@tei.no", saved.email());
        assertEquals("-", saved.fintStatus());
        assertEquals("ACTIVE", saved.entraStatus());
    }

    @Test
    void shouldSetDisabledWhenAccountIsNotEnabled() {
        String key = "f37f3048-637a-11ee-8c99-0242ac120002";

        ExternalUserPayload payload = new ExternalUserPayload(
                "Titten",
                "Tei",
                "EXTERNAL",
                "titten@tei.no",
                "Org",
                "198",
                null,
                "titten@tei.no",
                false
        );

        externalUserService.convertAndSaveAsUser(key, payload);

        ArgumentCaptor<FactoryUser> captor = ArgumentCaptor.forClass(FactoryUser.class);
        verify(userService).save(eq(key), captor.capture());

        FactoryUser saved = captor.getValue();
        assertEquals("Tei (ekstern)", saved.lastName());
        assertEquals("DISABLED", saved.entraStatus());
    }
}