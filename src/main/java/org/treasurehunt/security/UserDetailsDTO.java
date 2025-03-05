package org.treasurehunt.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Data Transfer Object (DTO) that implements {@link UserDetails} to represent user authentication details.
 * <p>This DTO is used for <b>authentication</b> purposes without exposing the full User entity.</p>
 *
 * @author Rashed Al Maaitah
 * @version 1.0
 */
public class UserDetailsDTO implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsDTO(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = List.copyOf(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.copyOf(authorities);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}