package no.fintlabs.externalUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import no.fintlabs.user.FactoryUser;
import no.fintlabs.user.User;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ExternalUser {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
    private UUID userObjectId;
    private String mainOrganisationUnitName;
    private String mainOrganisationUnitId;
    private String mobilePhone;
    private String email;
    private String userPrincipalName;
    private boolean accountEnabled;

    public FactoryUser toFactoryUser(){
        String lastnameSuffix = email != null && !email.isEmpty() ? " (ekstern " + email.split("@")[1] + ")" : " (ekstern)";
        return FactoryUser
                .builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName + lastnameSuffix)
                .userType("EXTERNAL")
                .resourceId(String.valueOf(userObjectId))
                .userName(userName)
                .identityProviderUserObjectId(userObjectId)
                .mainOrganisationUnitName(mainOrganisationUnitName)
                .mainOrganisationUnitId(mainOrganisationUnitId)
                .email(email)
                .fintStatus("-")
                .entraStatus(accountEnabled?"ACTIVE" :"DISABLED")
                .build();
    }

}


