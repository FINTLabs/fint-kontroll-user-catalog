package no.fintlabs.user;

import no.fint.antlr.FintFilterService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class ResponseFactory {

    private final FintFilterService fintFilterService;
    private final UserRepository userRepository;

    public ResponseFactory(FintFilterService fintFilterService, UserRepository userRepository) {
        this.fintFilterService = fintFilterService;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(
            FintJwtEndUserPrincipal principal,
            String filter,
            int page,
            int size) {
        Stream<User> userStream = userRepository.findAll().stream();
        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(
                        StringUtils.hasText(filter)
                                ? fintFilterService
                                .from(userStream, filter)
                                .map(User::toSimpleUser).toList()
                                : userStream.map(User::toSimpleUser).toList(),
                        PageRequest.of(page, size)
                )
        );

        return entity;
    }

    private Page<SimpleUser> toPage(List<SimpleUser> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
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
}
