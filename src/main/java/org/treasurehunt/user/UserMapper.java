package org.treasurehunt.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.treasurehunt.auth.CreateUserRequest;
import org.treasurehunt.auth.UserAuthResponse;
import org.treasurehunt.user.repository.entity.Role;
import org.treasurehunt.user.repository.entity.User;

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

    String[] map(Set<Role> roles) {
        return roles
                .stream()
                .map(role -> role.getId().getRoleName())
                .toArray(String[]::new);
    }

}