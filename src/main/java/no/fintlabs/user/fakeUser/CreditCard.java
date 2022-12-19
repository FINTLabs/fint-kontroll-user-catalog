
package no.fintlabs.user.fakeUser;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreditCard {

    @JsonProperty("card_cvv")
    private String cardCvv;
    @JsonProperty("card_expire")
    private String cardExpire;
    @JsonProperty("card_number")
    private String cardNumber;
    @JsonProperty("card_provider")
    private String cardProvider;
}
