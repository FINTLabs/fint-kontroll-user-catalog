package no.fintlabs.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    List<User> findUsersByUserTypeEquals(String userType);

    Optional<User> findUserByResourceIdEqualsIgnoreCase(String resourceId);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.mainOrganisationUnitId in ?2
                and u.userType = ?3
            order by u.lastName, u.firstName
            """)
    List<User> findUsersByNameOrgType(String searchName, Collection<String> mainOrganisationUnitIds, String userType);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.mainOrganisationUnitId in ?2
            order by u.lastName, u.firstName
            """)
    List<User> findUsersByNameOrg(String searchName, Collection<String> mainOrganisationUnitIds);


    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
                and u.userType = ?2
            order by u.lastName, u.firstName
            """)
    List<User> findUsersByNameType(String searchName,String userType);

    @Query("""
            select u from User u
            where
                upper(u.firstName || ' ' || u.lastName) like upper(concat('%', ?1, '%'))
            order by u.lastName, u.firstName
            """)
    List<User> findUsersByName(String searchName);


}
