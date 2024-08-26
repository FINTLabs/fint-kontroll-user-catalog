package no.fintlabs.user;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.opa.model.OrgUnitType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class UserSpesificationBuilder {

    private final String search;
    private final List<String> orgUnits;
    private final String userType;

    public UserSpesificationBuilder(String search, List<String> orgUnits, String userType) {
        this.search = search;
        this.orgUnits = orgUnits;
        this.userType = userType;
    }

    public Specification<User> build() {
        Specification<User> userSpec;

        if (orgUnits.contains(OrgUnitType.ALLORGUNITS.name())) {
            userSpec = Specification.where(null);
        } else {
            userSpec = allAutorizedOrgUnits(orgUnits);
        }

        if (!search.isEmpty()) {
            userSpec = userSpec.and(usersNameLike(search));
        }
        if (!userType.equals("ALLTYPES")) {
            userSpec = userSpec.and(userTypeEquals(userType.toLowerCase()));
        }

        return userSpec;
    }

    private Specification<User> allAutorizedOrgUnits(List<String> orgUnits) {

        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get("mainOrganisationUnitId")).value(orgUnits);
    }

    private Specification<User> userTypeEquals(String userType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(criteriaBuilder.lower(root.get("userType")), userType);
    }


    private Specification<User> usersNameLike(String search) {
        return (root, query, criteriaBuilder) -> {
            String[] searchParts = search.toLowerCase().split("\\s+");

            List<Predicate> predicates = new ArrayList<>();

            for (String part : searchParts) {
                String searchPattern = "%" + part + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
