package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.dto.UserDTOService;
import no.fintlabs.dto.UserDTOforDetails;
import no.fintlabs.dto.UserDTOforList;
import no.fintlabs.member.MemberService;
import no.fintlabs.repository.UserRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    private final MemberService memberService;
    private final FintFilterService fintFilterService;

    private final UserDTOService userDTOService;

    public UserService(UserRepository userRepository, MemberService memberService, FintFilterService fintFilterService, UserDTOService userDTOService) {
        this.userRepository = userRepository;
        this.memberService = memberService;
        this.fintFilterService = fintFilterService;
        this.userDTOService = userDTOService;
    }

    public void save(User user) {

        userRepository
                .findUserByResourceIdEqualsIgnoreCase(user.getResourceId())
                .ifPresentOrElse(onSaveExistingUser(user), onSaveNewUser(user));


    }

    private Runnable onSaveNewUser(User user) {
        return () -> {
            User newUser = userRepository.save(user);
            memberService.process(memberService.create(newUser));
        };
    }

    private Consumer<User> onSaveExistingUser(User user) {
        return existingUser -> {
            user.setId(existingUser.getId());
            memberService.process(memberService.create(user));
            userRepository.save(user);
        };
    }

    public Mono<UserDTOforDetails> getUserDTOforDetailsById(
            FintJwtEndUserPrincipal principal,
            Long id) {
        return Mono.just(userRepository.findById(id)
                .map(userDTOService::convertoDTOforDetails).orElse(new UserDTOforDetails()));
    }


    public ResponseEntity<Map<String, Object>> getAllUserPagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<UserDTOforList> userDTOforListPage;
        userDTOforListPage = userRepository.findAll(paging)
                .map(userDTOService::convertToDTOforList);

        return createResponsForPaging(userDTOforListPage);
    }

    public ResponseEntity<Map<String, Object>> getAllUserByTypePagedAndSorted(
            FintJwtEndUserPrincipal principal,
            int page,
            int size,
            String usertype) {

        Pageable paging = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<UserDTOforList> userDTOforListPage = userRepository.findUsersByUserTypeEquals(paging, usertype)
                .map(userDTOService::convertToDTOforList);
        return createResponsForPaging(userDTOforListPage);
    }


    public ResponseEntity<Map<String, Object>> getAllUsersStream(FintJwtEndUserPrincipal principal, String filter, int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        Stream<User> allUsers = userRepository.findAll().stream();
        List<UserDTOforList> filteredDTOsForList = fintFilterService.from(allUsers, filter)
                .map(userDTOService::convertToDTOforList).toList();
        Page<UserDTOforList> pagedAndFilteredDTOsForList = fromListToPage(filteredDTOsForList, paging);

        return createResponsForPaging(pagedAndFilteredDTOsForList);
    }

    private Page<UserDTOforList> fromListToPage(List<UserDTOforList> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());
        if (start > list.size())
            return new PageImpl<>(new ArrayList<>(), paging, list.size());
        return new PageImpl<>(list.subList(start, end), paging, list.size());
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
