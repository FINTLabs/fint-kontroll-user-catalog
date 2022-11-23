package no.fintlabs.model;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Data
@Slf4j
@Entity(name = "rolerefs")
@Table(name= "rolerefs")
public class RoleRefs {
    @Id
    private String userId;
    private String roleRefs;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;
}
