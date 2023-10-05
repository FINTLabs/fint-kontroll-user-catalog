package no.fintlabs.externalUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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
    private UUID identityProviderUserObjectId;
    private String mainOrganisationUnitName;
    private String mainOrganisationUnitId;
    private String mobilePhone;
    private String email;

    public boolean isValid(){
        return true;
    }

    public SimpelExternalUser toSimpleExternalUser() {
        return SimpelExternalUser
                .builder()
                .id(id)
                .fullName(firstName + " " + lastName)
                .userType(userType)
                .organisationUnitName(mainOrganisationUnitName)
                .organisationUnitId(mainOrganisationUnitId)
                .build();
    }
}


