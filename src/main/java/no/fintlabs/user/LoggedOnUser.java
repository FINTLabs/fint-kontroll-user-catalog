package no.fintlabs.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoggedOnUser {
    String firstName;
    String lastName;
    String organisationId;
    String mail;
}
