package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.User;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class UserService {
    int x = 0;
    public void process(User user) {
      //log.info(user.getUserId());
      log.info(String.valueOf(x++));
    }
}
