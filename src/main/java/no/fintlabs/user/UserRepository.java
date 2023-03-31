package no.fintlabs.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findUsersByUserTypeEquals(String userType);

    Optional<User> findUserByResourceIdEqualsIgnoreCase(String resourceId);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.mainOrganisationUnitId in ?2
                and u.userType = ?3
            """)
    List<User> findUsersByNameOrgType(String firstName, String lastName, Collection<String> mainOrganisationUnitIds, String userType);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.mainOrganisationUnitId in ?2
            """)
    List<User> findUsersByNameOrg(String firstName, String lastName, Collection<String> mainOrganisationUnitIds);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.userType = ?2
            """)
    List<User> findUsersByNameType(String firstName, String lastName,String userType);


}
