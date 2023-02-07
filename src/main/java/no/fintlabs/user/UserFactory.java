package no.fintlabs.user;

public class UserFactory {

    public static SimpleUser toSimpleUser(User user) {
        return SimpleUser
                .builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userType(user.getUserType())
                .organisationUnitName(user.getOrganisationUnitName())
                .build();
    }

    public static DetailedUser toDetailedUser(User user) {
        return DetailedUser
                .builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userName(user.getUserName())
                .organisationUnitName(user.getOrganisationUnitName())
                .mobilePhone(user.getMobilePhone())
                .email(user.getEmail())
                .build();
    }
}
