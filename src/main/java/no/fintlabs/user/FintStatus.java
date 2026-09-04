package no.fintlabs.user;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FintStatus {
    /**
     * Source-data validity from FINT. This is not the final catalog status; the
     * catalog combines it with Entra status and validity dates.
     */
    public static final String VALID = "VALID";
    public static final String INVALID = "INVALID";
}
