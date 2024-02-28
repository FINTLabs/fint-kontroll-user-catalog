package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Slf4j
public class UserSpesificationBuilder {
    private final String search;
    private final List<String> orgUnits; //lovlige orgunits
    private final String userType;

    public UserSpesificationBuilder(String search, List<String> orgUnits, String userType) {
        this.search = search;
        this.orgUnits = orgUnits;
        this.userType = userType;
    }

    public Specification<User> build() {
        Specification<User> userSpec = allAutorizedOrgUnits(orgUnits) ;

        if (!search.isEmpty()){
            userSpec = userSpec.and(usersNameLike(search));
        }
        if (!userType.equals("ALLTYPES")) {
            userSpec = userSpec.and(userTypeEquals(userType.toLowerCase()));
        }

        return userSpec;
    }

    private Specification<User> allAutorizedOrgUnits(List<String> orgUnits) {

        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get("mainOrganisationUnitId")).in(orgUnits);
    }

    private Specification<User> userTypeEquals(String userType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(criteriaBuilder.lower(root.get("userType")),userType);
    }

    private Specification<User> usersNameLike(String search) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + search + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + search + "%")
                );

    }


}
