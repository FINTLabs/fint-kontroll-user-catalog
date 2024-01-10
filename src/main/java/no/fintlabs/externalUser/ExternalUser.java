package no.fintlabs.externalUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
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
    private UUID idpUserObjectId;
    private String mainOrganisationUnitName;
    private String mainOrganisationUnitId;
    private String mobilePhone;
    private String email;
    private String userPrincipalName;
    private boolean accountEnabled;


    public boolean isValid(){
        return this.getUserType().equals("EXTERNAL");
    }

    public User toUser(){

        return User
                .builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName + " (ekstern)")
                .userType("EXTERNAL")
                .resourceId(String.valueOf(idpUserObjectId))
                .userName(userName)
                .identityProviderUserObjectId(idpUserObjectId)
                .mainOrganisationUnitName(mainOrganisationUnitName)
                .mainOrganisationUnitId(mainOrganisationUnitId)
                .email(email)
                .mobilePhone(mobilePhone)
                .build();
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

    public DetailedExternalUser toDetailedExternalUser(){
        return DetailedExternalUser
                .builder()
                .id(id)
                .fullName(firstName +" "+ lastName)
                .organisationUnitName(mainOrganisationUnitName)
                .mobilePhone(mobilePhone)
                .email(email)
                .build();
    }
}


