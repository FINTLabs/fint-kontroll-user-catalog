package no.fintlabs.user;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
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
@Table(name = "\"users\"",schema = "public")
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


//    @Column(name="organisationunitname")
//    private String organisationUnitName;
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
