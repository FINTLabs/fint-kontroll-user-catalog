package no.fintlabs.externalUser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DetailedExternalUser {
    private Long id;
    private String fullName;
    private String userName;
    private String organisationUnitName;
    private String mobilePhone;
    private String email;
}
