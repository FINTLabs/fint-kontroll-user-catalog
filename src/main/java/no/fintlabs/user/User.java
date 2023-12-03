package no.fintlabs.user;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Entity
@Table(name = "\"users\"")
public class User {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="resourceid",unique = true)
    private String resourceId;
    @Column(name = "firstname")
    private String firstName;
    @Column(name="lastname")
    private String lastName;
    @Column(name="usertype")
    private String userType;
    @Column(name="username")
    private String userName;
    @Column(name = "identityprovideruserobjectid")
    private UUID identityProviderUserObjectId;
    @Column(name="mainorganisationunitname")
    private String mainOrganisationUnitName;
    @Column(name="mainorganisationunitid")
    private String mainOrganisationUnitId;
    @ElementCollection
    private List<String> organisationUnitIds = new ArrayList<>();

    @Column(name="mobilephone")
    private String mobilePhone;
    @Column(name ="email")
    private String email;
    @Column(name="managerref")
    private  String managerRef;

    public SimpleUser toSimpleUser() {
        return SimpleUser
                .builder()
                .id(id)
                .fullName(firstName + " " + lastName)
                .userType(userType)
                .organisationUnitName(mainOrganisationUnitName)
                .organisationUnitId(mainOrganisationUnitId)
                .build();
    }

    public DetailedUser toDetailedUser() {
        return DetailedUser
                .builder()
                .id(id)
                .fullName(firstName + " " + lastName)
                .userName(userName)
                .organisationUnitName(mainOrganisationUnitName)
                .mobilePhone(mobilePhone)
                .email(email)
                .build();
    }

}
