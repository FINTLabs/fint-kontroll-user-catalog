package no.fintlabs.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class UserDTOforList {

    private Long id;
    private String fullName;
    private String organisationUnitName;
    private String userType;
}
