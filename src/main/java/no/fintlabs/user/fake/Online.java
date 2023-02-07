
package no.fintlabs.user.fake;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class Online {

    private String email;
    private String ipv4;
    private String ipv6;
    private String password;
    @JsonProperty("user_agent")
    private String userAgent;
    private String username;
    private String website;


    }


