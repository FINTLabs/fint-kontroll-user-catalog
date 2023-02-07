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

    public ResponseEntity<Map<String, Object>> toResponseEntity(FintJwtEndUserPrincipal principal, String filter, int page, int size) {

        return toResponseEntity(principal, filter, null, page, size);
//        return toResponseEntity(
//                toPage(
//                        fintFilterService
//                                .from(userRepository.findAll().stream(), filter)
//                                .map(UserFactory::toSimpleUser).toList(),
//                        PageRequest.of(page, size)
//                )
//        );
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(FintJwtEndUserPrincipal principal, String filter, String userType, int page, int size) {
        Stream<User> userStream = StringUtils.hasText(userType) ? userRepository.findUsersByUserTypeEquals(userType).stream() : userRepository.findAll().stream();
        return toResponseEntity(
                toPage(
                        fintFilterService
                                .from(userStream, filter)
                                .map(UserFactory::toSimpleUser).toList(),
                        PageRequest.of(page, size)
                )
        );
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
