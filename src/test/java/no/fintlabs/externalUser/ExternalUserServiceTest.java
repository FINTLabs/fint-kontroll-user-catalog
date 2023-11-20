package no.fintlabs.externalUser;

import no.fintlabs.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExternalUserServiceTest {
    private ExternalUserService externalUserService;

    @Test
    public void shouldReturnUser() {
        ExternalUser externalUser = ExternalUser
                .builder()
                .userName("titten@tei.no")
                .idpUserObjectId(UUID.fromString("f37f3048-637a-11ee-8c99-0242ac120002"))
                .userType("EXTERNAL")
                .build();

        User userToBeConverted = User
                .builder()
                .userName("titten@tei.no")
                .identityProviderUserObjectId(UUID.fromString("f37f3048-637a-11ee-8c99-0242ac120002"))
                .userType("EXTERNAL")
                .resourceId("f37f3048-637a-11ee-8c99-0242ac120002")
                .build();

        User convertedUser = externalUser.toUser();

        assertEquals(userToBeConverted.getResourceId(),convertedUser.getResourceId());
    }


}