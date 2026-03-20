package no.fintlabs.user;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Slf4j
@Entity
@Table(name = "users")
@EqualsAndHashCode
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
    @Builder.Default
    private Set<String> organisationUnitIds = new HashSet<>();
    @Column(name ="email")
    private String email;
    @Column(name="managerref")
    private  String managerRef;
    @Column(name = "status")
    private String status;
    @Column(name = "statuschanged")
    private Date statusChanged;
    @Column(name="validfrom")
    private Date validFrom;
    @Column(name="validto")
    private Date validTo;
    @CreationTimestamp
    @Column(name = "createddate", updatable = false)
    private Instant createdDate;
    @UpdateTimestamp
    @Column(name = "modifieddate")
    private Instant modifiedDate;

    public SimpleUser toSimpleUser() {
        return SimpleUser
                .builder()
                .id(id)
                .fullName(Stream.of(firstName, lastName).filter(Objects::nonNull).collect(Collectors.joining(" ")))
                .userType(userType)
                .organisationUnitName(mainOrganisationUnitName)
                .organisationUnitId(mainOrganisationUnitId)
                .userName(userName)
                .build();
    }

    public DetailedUser toDetailedUser() {
        return DetailedUser
                .builder()
                .id(id)
                .fullName(Stream.of(firstName, lastName).filter(Objects::nonNull).collect(Collectors.joining(" ")))
                .userName(userName)
                .organisationUnitName(mainOrganisationUnitName)
                .email(email)
                .userType(userType)
                .build();
    }

}
