package no.fintlabs.user;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AccessRequest {
    private List<UrlMethodPair> accessRequests;

    @Setter
    @Getter
    public static class UrlMethodPair {
        private String url;
        private String method;

        public UrlMethodPair(String path, String get) {
            this.url = path;
            this.method = get;
        }

    }
}
