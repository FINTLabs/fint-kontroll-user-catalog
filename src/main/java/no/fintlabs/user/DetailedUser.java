package no.fintlabs.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedUser {
    private Long id;
    private String fullName;
    private String userName;
    private String organisationUnitName;
    private String mobilePhone;
    private String email;

    @JsonIgnore
    public boolean isValid(){
        return this.id != null;
    }

}
