package no.fintlabs.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.user.fakeUser.SynthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@ConditionalOnProperty(value = "fint.kontroll.user-catalog.load-test-users", havingValue = "true")
//@PropertySource("classpath:application-nokafka.yaml")
@Service
public class TestUserService {
    private final UserService userService;

    private final WebClient webClient;
    @Value("${fint.kontroll.user-catalog.synthetic-user-token}")
    String synthUserToken;

    @Value("${fint.kontroll.user-catalog.number-of-test-users}")
    int numberOfTestUsers;

    public TestUserService(UserService userService) {
        this.userService = userService;


        this.webClient = WebClient.builder()
                .baseUrl("https://mmoapi.com/")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(
                                ConnectionProvider
                                        .builder("laidback")
                                        .maxLifeTime(Duration.ofMinutes(30))
                                        .maxIdleTime(Duration.ofMinutes(5))
                                        .build())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
                        .responseTimeout(Duration.ofMinutes(5))
                        .wiretap(true)))

                .build();
    }



    private static String getRandomUserType()
    {
        Random random = new Random();
        List<String> list = new ArrayList<>();
        list.add("EMPLOYEE");
        list.add("STUDENT");
        return list.get(random.nextInt(list.size()));
    }


    @PostConstruct
    public void init() throws JsonProcessingException, InterruptedException {



        for (int i = 0; i < numberOfTestUsers; i++) {

            String response = webClient.get()
                    .uri("api/contact-generator?localization=no_NO&token="+synthUserToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    //.map(s -> log.info(s))
                    .block();
            SynthUser synthUser = new ObjectMapper().readValue(response, SynthUser.class);

            log.info("{}", synthUser);

            userService.save(User.builder()
                            .resourceId("https://beta.felleskomponent.no/administrasjon/personal/personalressurs/ansattnummer/"
                            + synthUser.getSsn())
                            .firstName(synthUser.getFirstname())
                            .lastName(synthUser.getLastname())
                            .userType(getRandomUserType())
                            .userName(synthUser.getOnline().getUsername())
                            .identityProviderUserObjectId(UUID.randomUUID())
                            .mobilePhone(synthUser.getPhoneNumber())
                            .email(synthUser.getOnline().getEmail())
                            .managerRef("https://beta.felleskomponent.no/administrasjon/personal/personalressurs/ansattnummer/111-22-3333")
                    .build());

            Thread.sleep(1000);
        }


    }



}
