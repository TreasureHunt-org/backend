package org.treasurehunt.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.Role;
import org.treasurehunt.user.repository.entity.RoleId;
import org.treasurehunt.user.repository.entity.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        String password = "password123";
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(password);
        
        // Create a role for the user
        Role role = new Role();
        RoleId roleId = new RoleId();
        roleId.setUserId(userId);
        roleId.setRoleName(Roles.HUNTER.name());
        role.setId(roleId);
        role.setUser(user);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Act
        UserDetailsDTO userDetails = userDetailsService.loadUserByUsername(email);
        
        // Assert
        assertNotNull(userDetails);
        assertEquals(userId, userDetails.getId());
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Roles.HUNTER.name())));
        
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });
        
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithMultipleRoles_ShouldMapAllRoles() {
        // Arrange
        String email = "admin@example.com";
        Long userId = 2L;
        String password = "adminpass";
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(password);
        
        // Create multiple roles for the user
        Set<Role> roles = new HashSet<>();
        
        Role hunterRole = new Role();
        RoleId hunterRoleId = new RoleId();
        hunterRoleId.setUserId(userId);
        hunterRoleId.setRoleName(Roles.HUNTER.name());
        hunterRole.setId(hunterRoleId);
        hunterRole.setUser(user);
        roles.add(hunterRole);
        
        Role adminRole = new Role();
        RoleId adminRoleId = new RoleId();
        adminRoleId.setUserId(userId);
        adminRoleId.setRoleName(Roles.ADMIN.name());
        adminRole.setId(adminRoleId);
        adminRole.setUser(user);
        roles.add(adminRole);
        
        user.setRoles(roles);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Act
        UserDetailsDTO userDetails = userDetailsService.loadUserByUsername(email);
        
        // Assert
        assertNotNull(userDetails);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Roles.HUNTER.name())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Roles.ADMIN.name())));
        
        verify(userRepository).findByEmail(email);
    }
}