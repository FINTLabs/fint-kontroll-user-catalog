package no.fintlabs.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import no.fintlabs.opa.model.AuthRole;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoggedOnUser {
    String firstName;
    String lastName;
    //TODO: Should be called organizationId
    String organisationId;
    String mail;
    List<AuthRole> roles;
}
