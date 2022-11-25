package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.User;
import no.fintlabs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    int x = 0;
    public void process(User user) {
      //log.info(user.getUserId());
      log.info(String.valueOf(x++));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
