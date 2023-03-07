package no.fintlabs.user

import spock.lang.Specification

class UserSpec extends Specification {

    def "Converting a User to simple user should return a simple user with values from the User"() {
        given:
        def user = User.builder()
        .id(1000L)
        .firstName("firstname")
        .lastName("lastname")
        .mainOrganisationUnitName("org")
        .mainOrganisationUnitId("1234")
        .userType("userType")
                .build()

        when:
        def simpleUser = user.toSimpleUser()

        then:
        simpleUser.getFullName().endsWith(user.getLastName())
        simpleUser.getId() == user.getId()
        simpleUser.getOrganisationUnitName() == user.getMainOrganisationUnitName()
        simpleUser.getOrganisationUnitId() == user.getMainOrganisationUnitId()
        simpleUser.getUserType() == user.getUserType()

    }

    def "Converting a User to detailed user with values from the User"(){
        given:
        def user = User.builder()
        .id(4711L)
        .firstName("firstname")
        .lastName("lastname")
        .userName("brukernavn@org.no")
        .mainOrganisationUnitName("orgenheten")
        .userType("EMPLOYEE")
        .mobilePhone("12345678")
        .email("brukernavn@org.no")
        .build()

        when:
        def detaildUser = user.toDetailedUser()

        then:
        detaildUser.getFullName().endsWith(user.getLastName())
        detaildUser.getUserName() == user.getUserName()
        detaildUser.getOrganisationUnitName() == user.getMainOrganisationUnitName()
        detaildUser.getMobilePhone() == user.getMobilePhone()
        detaildUser.getEmail() == user.getEmail()
    }
}
