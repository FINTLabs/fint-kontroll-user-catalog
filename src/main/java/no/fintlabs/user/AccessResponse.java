package no.fintlabs.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccessResponse {
    private String url;
    private boolean access;

    public AccessResponse(String url, boolean access) {
        this.url = url;
        this.access = access;
    }

}
