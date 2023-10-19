package no.fintlabs.member;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class Member {
    private String resourceId;
    private Long id;
    private String firstName;
    private String lastName;
    private String userType;
    private String UserName;
    private UUID identityProviderUserObjectId;
}
