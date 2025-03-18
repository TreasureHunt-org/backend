package org.treasurehunt.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.treasurehunt.common.api.ApiResponse;
import org.treasurehunt.common.api.PageDTO;
import org.treasurehunt.common.api.PageResponse;
import org.treasurehunt.user.repository.UserSearchCriteria;
import org.treasurehunt.user.repository.entity.User;

import java.util.List;

import static org.treasurehunt.common.constants.PathConstants.USER_BASE;

@RestController
@RequestMapping(USER_BASE)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<User>>> getAllUsers(
            PageDTO pageDTO,
            UserSearchCriteria searchCriteria
    ) {

        // TODO implement this method
        PageResponse<User> userPageResponse = userService.searchUsers(pageDTO, searchCriteria);

        return ResponseEntity.ok(
                ApiResponse.success(
                        List.of(userPageResponse),
                        "Fetched Users successfully"
                )
        );
    }
}
