package no.fintlabs.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Data
@Slf4j
@Entity
@Table(name = "\"users\"",schema = "fylke")
public class User {
    @Id
    @Column(name="userid")
    private String userId;
    @Column(name = "firstname")
    private String firstName;
    @Column(name="lastname")
    private String lastName;
    @Column(name="usertype")
    private String userType;
    @Column(name="username")
    private String userName;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<RoleRefs> roleRefs = new ArrayList<>();
    @Column(name="mobilephone")
    private String mobilePhone;
    @Column(name ="email")
    private String email;
    @Column(name="managerref")
    private  String managerRef;

}
