package no.fintlabs.user;

import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UserStatus {
    public static final String ACTIVE = "ACTIVE";
    public static final String DISABLED = "DISABLED";
    public static final String INVALID = "INVALID";
    public static final String DELETED = "DELETED";

    public static final Set<String> VALID_STATUSES = Set.of(ACTIVE, DISABLED);
}
