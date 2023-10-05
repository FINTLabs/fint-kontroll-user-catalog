package no.fintlabs.externalUser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpelExternalUser {
    private Long id;
    private String fullName;
    private String organisationUnitName;
    private String organisationUnitId;
    private String userType;

}
