
package no.fintlabs.user.fakeUser;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.Generated;


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


