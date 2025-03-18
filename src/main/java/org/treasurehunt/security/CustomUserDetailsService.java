package org.treasurehunt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.treasurehunt.user.repository.entity.Role;
import org.treasurehunt.user.repository.entity.User;
import org.treasurehunt.user.repository.UserRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service impl for loading user-specific data.
 * This class implements {@link UserDetailsService} to provide user auth details
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsDTO loadUserByUsername(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow((() -> new UsernameNotFoundException("Email Not found")));

        return new UserDetailsDTO(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    /**
     * Maps a set of roles to a collection of granted authorities.
     *
     * @param roles the set of roles assigned to the user.
     * @return a collection of {@link GrantedAuthority} representing the user's roles.
     */
    private Collection<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getId().getRoleName()))
                .collect(Collectors.toSet());
    }
}