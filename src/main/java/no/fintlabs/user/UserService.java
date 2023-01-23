package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.dto.UserDTOService;
import no.fintlabs.dto.UserDTOforDetails;
import no.fintlabs.dto.UserDTOforList;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.repository.UserRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;


@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberService memberService;
    @Autowired
    private FintFilterService fintFilterService;

    @Autowired
    private UserDTOService userDTOService;

    public User save(User user) {
        String userResourceId = user.getResourceId();
        User existingUser = userRepository.findUserByResourceIdEqualsIgnoreCase(userResourceId).orElse(null);
        if (existingUser == null) {
            User newUser = userRepository.save(user);
            Member newMember = memberService.create(newUser);
            memberService.process(newMember);
            return newUser;
        } else {
            user.setId(existingUser.getId());
            Member member = memberService.create(user);
            memberService.process(member);
            return userRepository.save(user);

        }
    }

    public Mono<UserDTOforDetails> getUserDTOforDetailsById(
            FintJwtEndUserPrincipal principal,
            Long id){
        return Mono.just(userRepository.findById(id)
                .map(s->userDTOService.convertoDTOforDetails(s)).orElse(new UserDTOforDetails()));
    }


    public ResponseEntity<Map<String, Object>> getAllUserDTOsPagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<UserDTOforList> userDTOforListPage;
        userDTOforListPage = userRepository.findAll(paging)
                .map(s->userDTOService.convertToDTOforList(s));

        return createResponsForPaging(userDTOforListPage);
    }

    public ResponseEntity<Map<String, Object>> getAllUserDTOsByTypePagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size,
            String usertype) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<UserDTOforList> userDTOforListPage = userRepository.findUsersByUserTypeEquals(paging,usertype)
                .map(s-> userDTOService.convertToDTOforList(s));
        return createResponsForPaging(userDTOforListPage);

    }



    public Flux<User> getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal from, String firstnamepart) {
        List<User> allUsers = userRepository.findAllUsersByStartingFirstnameWith(firstnamepart);
        return Flux.fromIterable(allUsers);
    }

    public Stream<User> getAllUsersStream(FintJwtEndUserPrincipal principal) {
        return userRepository.findAll().stream();
    }

    private ResponseEntity<Map<String, Object>> createResponsForPaging(Page<UserDTOforList> userPage) {
        List<UserDTOforList> content = userPage.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("totalItems", userPage.getTotalElements());
        response.put("users", content);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
