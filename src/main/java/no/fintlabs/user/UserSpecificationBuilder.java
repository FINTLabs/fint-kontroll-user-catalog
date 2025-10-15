package no.fintlabs.user;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class UserSpecificationBuilder {

    private final String search;
    private final List<String> orgUnits;
    private final List<String> userType;

    public UserSpecificationBuilder(String search, List<String> orgUnits, List<String> userType) {
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
            userSpec = userSpec.and(firstNameLastNameOrUserNameLike(search));
        }

        if (!userType.contains("ALLTYPES")) {
            userSpec = userSpec.and(userTypeEquals(userType));
        }
        return userSpec;
    }

    private Specification<User> allAutorizedOrgUnits(List<String> orgUnits) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get("mainOrganisationUnitId")).value(orgUnits);
    }

    private Specification<User> userTypeEquals(List<String> userType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get("userType")).value(userType);
    }


    private Specification<User> firstNameLastNameOrUserNameLike(String search) {
        return (root, query, criteriaBuilder) -> {
            String[] searchParts = search.toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>();

            for (String part : searchParts) {
                if (part.isBlank()) continue;
                String searchPattern = "%" + part + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("userName")), searchPattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
