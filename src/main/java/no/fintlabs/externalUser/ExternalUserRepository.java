package no.fintlabs.externalUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalUserRepository extends JpaRepository<ExternalUser,Long> {


    Optional<ExternalUser> findExternalUserByIdentityProviderUserObjectId(UUID uuid);


}
