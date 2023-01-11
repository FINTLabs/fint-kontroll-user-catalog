package no.fintlabs.dto;

import no.fintlabs.user.User;
import org.springframework.stereotype.Service;


@Service
public class UserDTOService {
    public UserDTOforList convertToDTOforList(User user){
        return UserDTOforList
                .builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userType(user.getUserType())
                .organisationUnitName(user.getOrganisationUnitName())
                .build();
    }

    public UserDTOforDetails convertoDTOforDetails(User user){
        return UserDTOforDetails
                .builder()
                .id(user.getId())
                .fullName(user.getFirstName() +" "+ user.getLastName())
                .userName(user.getUserName())
                .organisationUnitName(user.getOrganisationUnitName())
                .mobilePhone(user.getMobilePhone())
                .email(user.getEmail())
                .build();
    }
}
