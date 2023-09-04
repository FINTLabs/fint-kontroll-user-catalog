package no.fintlabs.user;


import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Import(SecurityTestConfig.class)
@WebFluxTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;


    @MockBean
    private ResponseFactory responseFactory;

    @MockBean
    private UserService userService;


    @Test
    void shouldGetUsers() {
        // TODO: Kanskje greit å ha noe slags brukere i denne listen?
        Map<String, Object> expextedRespons = new HashMap<>();
        expextedRespons.put("responsKey", "responsValue");

        given(responseFactory.toResponseEntity(any(FintJwtEndUserPrincipal.class),
                                               anyString(),
                                               anyList(),
                                               anyString(),
                                               anyInt(),
                                               anyInt()))
                .willReturn(ResponseEntity.ok(expextedRespons));

        List<String> orgUnits = Arrays.asList("unit1", "unit2", "unit3");

        webTestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/users");
                    uriBuilder.queryParam("search", "test");
                    uriBuilder.queryParam("orgUnits", orgUnits.toArray());
                    uriBuilder.queryParam("userType", "test");
                    uriBuilder.queryParam("page", 0);
                    uriBuilder.queryParam("size", 20);
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy jwt token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .isEqualTo(expextedRespons);
    }
}
