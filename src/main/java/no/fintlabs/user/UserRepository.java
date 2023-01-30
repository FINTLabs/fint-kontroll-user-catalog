package no.fintlabs.user;

import no.fintlabs.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Page<User> findUsersByUserTypeEquals(Pageable pageable, String userType);

    @Query("select u from User u where upper(u.firstName) like upper(concat(:firstName, '%'))")
    List<User> findAllUsersByStartingFirstnameWith(@Param("firstName") String firstName);

    Optional<User> findUserByResourceIdEqualsIgnoreCase(String resourceId);
}
