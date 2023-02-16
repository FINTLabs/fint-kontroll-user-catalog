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
        .userType("userType")
                .build()

        when:
        def simpleUser = user.toSimpleUser()

        then:
        simpleUser.getFullName().endsWith(user.getLastName())
        simpleUser.getId() == user.getId()
        simpleUser.getOrganisationUnitName() == user.getMainOrganisationUnitName()
        simpleUser.getUserType() == user.getUserType()

    }
}
