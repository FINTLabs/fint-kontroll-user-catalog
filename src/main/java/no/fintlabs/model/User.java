package no.fintlabs.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Entity(name="user")
@Table(name = "user")
public class User {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoleRefs> roleRefs = new ArrayList<>();
    private String mobilePhone;
    private String email;
    private  String managerRef;

}
