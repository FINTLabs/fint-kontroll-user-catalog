package no.fintlabs.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTOforDetails {
    private Long id;
    private String fullName;
    private String userName;
    private String organisationUnitName;
    private String mobilePhone;
    private String email;
}
