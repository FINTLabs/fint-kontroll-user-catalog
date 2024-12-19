package no.fintlabs.user;

import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ResponseFactory {

    private final UserService userService;

    public ResponseFactory(UserService userService) {
        this.userService = userService;
    }


    public ResponseEntity<Map<String,Object>> toResponseEntity(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            List<String> userType,
            int page,
            int size
    ) {
        List<SimpleUser> simpleUsers = userService.getSimpleUsersUsingSpec(search,orgUnits,userType);

        return toResponseEntity(
                toPage(simpleUsers,PageRequest.of(page, size)));
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(Page<SimpleUser> userPage) {

        return new ResponseEntity<>(
                Map.of("totalItems", userPage.getTotalElements(),
                        "users", userPage.getContent(),
                        "currentPage", userPage.getNumber(),
                        "totalPages", userPage.getTotalPages()
                ),
                HttpStatus.OK
        );
    }

    public ResponseEntity<Map<String,Object>> toResponseEntity(String message) {
        return new ResponseEntity<>(
                Map.of("message", message),
                HttpStatus.OK
        );
    }

    private Page<SimpleUser> toPage(List<SimpleUser> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }
}
