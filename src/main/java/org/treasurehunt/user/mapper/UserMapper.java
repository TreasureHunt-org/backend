package org.treasurehunt.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.treasurehunt.auth.CreateUserRequest;
import org.treasurehunt.auth.UserAuthResponse;
import org.treasurehunt.user.repository.entity.Role;
import org.treasurehunt.user.repository.entity.User;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "password",
            expression = "java( passwordEncoder.encode(request.password()) )")
    public abstract User toUser(CreateUserRequest request);

    public abstract UserAuthResponse toUserAuthResponse(User user);

    // Convert a list of Users to a list of UserAuthResponses
    public abstract List<UserAuthResponse> toUserAuthResponseList(List<User> users);

    // Convert Page<User> to Page<UserAuthResponse>
    public Page<UserAuthResponse> toUserAuthPageResponse(Page<User> users) {
        List<UserAuthResponse> userAuthResponses = toUserAuthResponseList(users.getContent());
        return new PageImpl<>(userAuthResponses, users.getPageable(), users.getTotalElements());
    }
    String[] map(Set<Role> roles) {
        return roles
                .stream()
                .map(role -> role.getId().getRoleName())
                .toArray(String[]::new);
    }

}