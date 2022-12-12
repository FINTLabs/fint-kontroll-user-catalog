package no.fintlabs.repository;

import no.fintlabs.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    @Query("select u from User u where upper(u.userType) = upper(?1)")
    List<User> findByUserType(String userType);


    @Query("select u from User u where upper(u.firstName) like upper(concat(:firstName, '%'))")
    List<User> findAllUsersByStartingFirstnameWith(@Param("firstName") String firstName);

    @Query("""
            select u from User u
            where upper(u.firstName) like upper(concat(:firstName, '%')) and upper(u.userType) = upper(:userType)""")
    List<User> findUsersByFirstNamePartAndIsStudent(@Param("firstName") String firstName, @Param("userType") String userType);

    @Query("select u from User u order by u.firstName")
    List<User> findAllUsersPagable(Pageable pageable);



}
