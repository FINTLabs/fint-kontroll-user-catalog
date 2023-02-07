package no.fintlabs.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findUsersByUserTypeEquals(String userType);

    Optional<User> findUserByResourceIdEqualsIgnoreCase(String resourceId);
}
