package no.fintlabs.externalUser;

import lombok.Builder;
import no.fintlabs.user.FactoryUser;

import java.util.UUID;
@Builder
public record ExternalUserPayload(String firstName,
                                  String lastName,
                                  String userType,
                                  String userName,
                                  String mainOrganisationUnitName,
                                  String mainOrganisationUnitId,
                                  String email,
                                  String userPrincipalName,
                                  boolean accountEnabled) {

    public FactoryUser toFactoryUser(String userObjectId) {
        String lastnameSuffix = email != null && !email.isEmpty() ? " (ekstern " + email.split("@")[1] + ")" : " (ekstern)";
        return FactoryUser
                .builder()
                .firstName(firstName)
                .lastName(lastName + lastnameSuffix)
                .userType("EXTERNAL")
                .resourceId(userObjectId)
                .userName(userName)
                .identityProviderUserObjectId(UUID.fromString(userObjectId))
                .mainOrganisationUnitName(mainOrganisationUnitName)
                .mainOrganisationUnitId(mainOrganisationUnitId)
                .email(email)
                .fintStatus("-")
                .entraStatus(accountEnabled?"ACTIVE" :"DISABLED")
                .build();
    }
}





