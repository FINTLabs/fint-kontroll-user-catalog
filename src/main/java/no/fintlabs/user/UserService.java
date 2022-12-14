package no.fintlabs.user;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.FintFilterService;
import no.fintlabs.model.User;
import no.fintlabs.repository.UserRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FintFilterService fintFilterService;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Flux<User> getAllUsers(FintJwtEndUserPrincipal principal) {
        List<User> allUsers  = userRepository .findAll().stream().collect(Collectors.toList());

        return Flux.fromIterable(allUsers);
    }

    public Stream<User> getAllUsersStream(FintJwtEndUserPrincipal principal) {
        return userRepository.findAll().stream();
    }

    public Flux<User> getAllUsersFirstnameStartingWith(FintJwtEndUserPrincipal from, String firstnamepart) {
        List<User> allUsers = userRepository.findAllUsersByStartingFirstnameWith(firstnamepart);
        return Flux.fromIterable(allUsers);
    }

    public ResponseEntity<Map<String, Object>> getAllUsersPaged(FintJwtEndUserPrincipal principal, int page, int size) {

        Pageable paging = PageRequest.of(page, size);
        Page<User> userPage;
        userPage = userRepository.findAllUsersPagable(paging);
        List<User> content = userPage.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("totalItems", userPage.getTotalElements());
        response.put("users", content);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Map<String, Object>> getFilteredAndPagedUsers(FintJwtEndUserPrincipal principal, String filter, int page, int size) {

        Stream<User> allUsers = userRepository.findAll().stream();
        List<User>filteredResult = fintFilterService.from(allUsers, filter).toList();

        Pageable paging = PageRequest.of(page, size);
        Page<User> userPage = fromListToPage(filteredResult,paging);


        List<User> content = userPage.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("totalItems", userPage.getTotalElements());
        response.put("users", content);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    private Page<User> fromListToPage(List<User> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());
        if(start > list.size())
            return new PageImpl<>(new ArrayList<>(), paging, list.size());
        return new PageImpl<>(list.subList(start, end), paging, list.size());
    }
}
