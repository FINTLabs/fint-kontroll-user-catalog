package no.fintlabs.user;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityContextRepository(securityContextRepository())
                .authorizeExchange()
                .anyExchange().permitAll();
        return http.build();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new MockServerSecurityContextRepository();
    }

    private static class MockServerSecurityContextRepository implements ServerSecurityContextRepository {

        @Override
        public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Mono<SecurityContext> load(ServerWebExchange exchange) {
            String tokenValue =
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" +
                    ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "1234567890");
            claims.put("name", "John Doe");

            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");
            headers.put("typ", "JWT");

            Jwt jwt = new Jwt(tokenValue, Instant.now(), Instant.now().plusSeconds(86400), headers, claims);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(new JwtAuthenticationToken(jwt, Collections.emptyList()));

            return Mono.just(securityContext);
        }
    }

    private static class JwtAuthenticationToken extends AbstractAuthenticationToken {

        private final Jwt jwt;

        public JwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
            super(authorities);
            this.jwt = jwt;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return jwt.getTokenValue();
        }

        @Override
        public Object getPrincipal() {
            return jwt;
        }
    }
}
