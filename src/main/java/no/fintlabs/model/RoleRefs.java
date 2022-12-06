package no.fintlabs.model;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Data
@Slf4j
@Entity
@Table(name= "rolerefs")
public class RoleRefs {
    @Id
    @Column(name="userid")
    private String userId;
    @Column(name="rolerefs")
    private String roleRefs;

//    @ManyToOne(fetch = FetchType.LAZY)
//    User user;
}
