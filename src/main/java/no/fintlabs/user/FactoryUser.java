package no.fintlabs.user;


import lombok.Builder;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Builder
public record FactoryUser(
        Long id,
        String resourceId,
        String firstName,
        String lastName,
        String userType,
        String userName,
        UUID identityProviderUserObjectId,
        String mainOrganisationUnitName,
        String mainOrganisationUnitId,
        Set<String> organisationUnitIds,
        String email,
        String managerRef,
        String fintStatus,
        Date validFrom,
        Date validTo,
        String entraStatus
) {
}