package no.fintlabs.externalUser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@Service
@Slf4j
public class ExternalUserService {
    private final ExternalUserRepository externalUserRepository;

    public ExternalUserService(ExternalUserRepository externalUserRepository) {
        this.externalUserRepository = externalUserRepository;
    }

    public void save(ExternalUser externalUser) {
        externalUserRepository
                .findExternalUserByIdentityProviderUserObjectId(externalUser.getIdentityProviderUserObjectId())
                .ifPresentOrElse(onSaveExistingExternalUser(externalUser), onSaveNewExternalUser(externalUser));
    }

    private Runnable onSaveNewExternalUser(ExternalUser externalUser){
        return ()-> {
          ExternalUser newExternalUser = externalUserRepository.save(externalUser);
          log.info("Created new external user" + newExternalUser.getUserName());
        };
    }

    private Consumer<ExternalUser> onSaveExistingExternalUser(ExternalUser externalUser){
        return existingExternalUser -> {
          externalUser.setId(existingExternalUser.getId());
          log.info("Updating external user " + externalUser.getUserName());
          externalUserRepository.save(externalUser);
        };
    }

    public Optional<ExternalUser> getExternalUserById(Long id){
        return externalUserRepository.findById(id);
    }

    public List<ExternalUser> getAllExternalUsers(){
        return null;
    }



}
