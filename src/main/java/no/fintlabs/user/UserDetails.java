package no.fintlabs.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetails {
    private Long id;
    private String fullName;
    private String userName;
    private String organisationUnitName;
    private String mobilePhone;
    private String email;
}
