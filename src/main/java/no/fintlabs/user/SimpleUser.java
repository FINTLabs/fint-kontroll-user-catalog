package no.fintlabs.user;

import lombok.*;

@Getter
@Setter
@Builder
public class SimpleUser {

    private Long id;
    private String fullName;
    private String organisationUnitName;
    private String userType;
}
