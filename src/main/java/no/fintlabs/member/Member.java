package no.fintlabs.member;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class Member {
    private String resourceId;
    private Long id;
}
