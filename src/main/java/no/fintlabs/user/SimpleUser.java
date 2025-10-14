package no.fintlabs.user;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SimpleUser {
    private Long id;
    private String fullName;
    private String organisationUnitName;
    private String organisationUnitId;
    private String userType;
    private String userName;
}
