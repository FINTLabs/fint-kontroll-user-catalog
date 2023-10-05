package no.fintlabs.externalUser;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@Service
@Slf4j
public class ExternalUserService {
    private final ExternalUserRepository externalUserRepository;
    private final UserService userService;

    public ExternalUserService(ExternalUserRepository externalUserRepository, UserService userService) {
        this.externalUserRepository = externalUserRepository;
        this.userService = userService;
    }

//    public void save(ExternalUser externalUser) {
//        externalUserRepository
//                .findExternalUserByIdentityProviderUserObjectId(externalUser.getIdentityProviderUserObjectId())
//                .ifPresentOrElse(onSaveExistingExternalUser(externalUser), onSaveNewExternalUser(externalUser));
//    }
//
//    private Runnable onSaveNewExternalUser(ExternalUser externalUser){
//        return ()-> {
//          ExternalUser newExternalUser = externalUserRepository.save(externalUser);
//          log.info("Created new external user" + newExternalUser.getUserName());
//        };
//    }
//
//    private Consumer<ExternalUser> onSaveExistingExternalUser(ExternalUser externalUser){
//        return existingExternalUser -> {
//          externalUser.setId(existingExternalUser.getId());
//          log.info("Updating external user " + externalUser.getUserName());
//          externalUserRepository.save(externalUser);
//        };
//    }
//
//    public Optional<ExternalUser> getExternalUserById(Long id){
//        return externalUserRepository.findById(id);
//    }
//
//    public List<ExternalUser> getAllExternalUsers(){
//        return null;
//    }


    public void convertAndSaveAsUser(ExternalUser externalUser) {
        User convertedExternalUser = externalUser.toUser();
        userService.save(convertedExternalUser);
    }
}
