package no.fintlabs.user;


import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@Import(SecurityTestConfig.class)
@WebFluxTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JwtDecoder jwtDecoder;

    @MockBean
    private ResponseFactory responseFactory;

    @MockBean
    private UserService userService;



    @Test
    void testEndpoint_whenUserScopesFound_shouldReturn200() {


     String jwtString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";



        Jwt jwtMock = jwtDecoder.decode(jwtString);
        Map<String,Object> expextedRespons = new HashMap<>();
        expextedRespons.put("responsKey","responsValue");

        given(responseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwtMock),"%",0,20))
                .willReturn(ResponseEntity.ok(expextedRespons));

        webTestClient.get()
                .uri("api/users")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtMock.getTokenValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .isEqualTo(expextedRespons);
    }

}
