package no.fintlabs.user;

import lombok.Builder;

import java.util.Date;
import java.util.UUID;

@Builder
public record KontrollUser(
        Long id,
        String resourceId,
        String firstName,
        String lastName,
        String userType,
        String userName,
        UUID identityProviderUserObjectId,
        String mainOrganisationUnitName,
        String mainOrganisationUnitId,
        String status,
        Date statusChanged
) {
}
