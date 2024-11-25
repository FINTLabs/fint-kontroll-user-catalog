package no.fintlabs.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.AuthRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationClient authorizationClient;

    @MockBean
    private UserService userService;

    @MockBean
    private ResponseFactory responseFactory;

    @MockBean
    private UserEntityProducerService userEntityProducerService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, responseFactory, authorizationClient, userEntityProducerService)).build();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void shouldGetLoggedOnUserInformation() throws Exception {
        String principalMail = "test@example.com";
        String role = "ROLE_USER";
        Jwt jwt = createFullMockJwtToken(role, principalMail, "Kjell", "Testersen", "89898989");
        createSecurityContext(jwt, role);

        when(authorizationClient.getUserRoles()).thenReturn(List.of(new AuthRole("sa", "Systemadministrator")));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kjell"))
                .andExpect(jsonPath("$.lastName").value("Testersen"))
                .andExpect(jsonPath("$.mail").value(principalMail))
                .andExpect(jsonPath("$.organisationId").value("89898989"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0].id").value("sa"))
                .andExpect(jsonPath("$.roles[0].name").value("Systemadministrator"));

        verify(authorizationClient, times(1)).getUserRoles();
    }

    @Test
    public void shouldAuthorizeMultipleRequestsSuccessfully() throws Exception {
        String principalMail = "test@example.com";
        String role = "ROLE_USER";
        Jwt jwt = createMockJwtToken(role, principalMail);
        createSecurityContext(jwt, role);

        AccessRequest.UrlMethodPair request1 = new AccessRequest.UrlMethodPair("/api/users/me", "GET");
        AccessRequest.UrlMethodPair request2 = new AccessRequest.UrlMethodPair("/api/admin", "POST");

        AccessRequest accessRequest = new AccessRequest();
        accessRequest.setAccessRequests(List.of(request1, request2));

        when(authorizationClient.isAuthorized(principalMail, "GET", "/api/users/me")).thenReturn(true);
        when(authorizationClient.isAuthorized(principalMail, "POST", "/api/admin")).thenReturn(false);

        String requestBody = new ObjectMapper().writeValueAsString(accessRequest);

        mockMvc.perform(post("/api/users/me/hasaccess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].url").value("/api/users/me"))
                .andExpect(jsonPath("$[0].access").value(true))
                .andExpect(jsonPath("$[1].url").value("/api/admin"))
                .andExpect(jsonPath("$[1].access").value(false));

        verify(authorizationClient, times(1)).isAuthorized(principalMail, "GET", "/api/users/me");
        verify(authorizationClient, times(1)).isAuthorized(principalMail, "POST", "/api/admin");
    }

    @Test
    public void shouldReturnEmptyResponseForEmptyAccessRequest() throws Exception {
        String principalMail = "test@example.com";
        String role = "ROLE_USER";
        Jwt jwt = createMockJwtToken(role, principalMail);
        createSecurityContext(jwt, role);

        AccessRequest accessRequest = new AccessRequest();
        accessRequest.setAccessRequests(Collections.emptyList());

        String requestBody = new ObjectMapper().writeValueAsString(accessRequest);

        mockMvc.perform(post("/api/users/me/hasaccess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verifyNoInteractions(authorizationClient);
    }


    @Test
    public void shouldReturnBadRequestForInvalidPayload() throws Exception {
        String principalMail = "test@example.com";
        String role = "ROLE_USER";
        Jwt jwt = createMockJwtToken(role, principalMail);
        createSecurityContext(jwt, role);

        String invalidRequestBody = "{ \"invalid\": \"data\" }";

        mockMvc.perform(post("/api/users/me/hasaccess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }


    private Jwt createMockJwtToken(String role, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("authenticated", role));
        claims.put("email", email);
        Jwt jwt = new Jwt("dummyToken", Instant.now(), Instant.now().plusSeconds(60), claims, claims);
        return jwt;
    }

    private Jwt createFullMockJwtToken(String role, String email, String givenName, String surName, String orgid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("authenticated", role));
        claims.put("email", email);
        claims.put("givenname", givenName);
        claims.put("surname", surName);
        claims.put("organizationid", orgid);
        Jwt jwt = new Jwt("dummyToken", Instant.now(), Instant.now().plusSeconds(60), claims, claims);
        return jwt;
    }

    private void createSecurityContext(Jwt jwt, String role) throws ServletException {
        SecurityContextHolder.getContext().setAuthentication(createJwtAuthentication(jwt, role));
        SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
        authInjector.afterPropertiesSet();
    }

    private UsernamePasswordAuthenticationToken createJwtAuthentication(Jwt jwt, String role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwt, null, authorities);
        return authentication;
    }
}
