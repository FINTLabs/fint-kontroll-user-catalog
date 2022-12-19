
package no.fintlabs.user.fakeUser;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SynthUser {

    private String address;
    private String avatar;
    private String birthdate;
    @JsonProperty("blood_group")
    private String bloodGroup;
    @JsonProperty("credit_card")
    private CreditCard creditCard;
    @JsonProperty("current_location")
    private List<Double> currentLocation;
    private Employment employment;
    private String firstname;
    private String fullname;
    private String gender;
    private String lastname;
    private Online online;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String ssn;


}
