package org.treasurehunt.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.auth.ChangePasswordRequest;
import org.treasurehunt.auth.CreateUserRequest;
import org.treasurehunt.auth.UserAuthResponse;
import org.treasurehunt.common.api.PageDTO;
import org.treasurehunt.common.api.PageResponse;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.exception.*;
import org.treasurehunt.security.jwt.JwtService;
import org.treasurehunt.user.mapper.UserMapper;
import org.treasurehunt.user.repository.UserCriteriaRepository;
import org.treasurehunt.user.repository.UserSearchCriteria;
import org.treasurehunt.user.repository.entity.Role;
import org.treasurehunt.user.repository.entity.RoleId;
import org.treasurehunt.user.repository.entity.User;
import org.treasurehunt.user.repository.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.treasurehunt.common.constants.UploadingConstants.ALLOWED_TYPES;
import static org.treasurehunt.common.constants.UploadingConstants.USER_UPLOAD_DIR;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserCriteriaRepository userCriteriaRepository;

    @Value("${app.security.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    // TODO change to a different DTO
    public PageResponse<UserAuthResponse> searchUsers(PageDTO pageDTO,
                                                      UserSearchCriteria searchCriteria) {
        return PageResponse.fromPage(userMapper.toUserAuthPageResponse(userCriteriaRepository.findAllWithFilters(pageDTO, searchCriteria)));
    }

    @Transactional
    public User updateRefreshToken(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(refreshTokenExpiration));

        return userRepository.save(user);
    }

    @Transactional
    public UserAuthResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmailOrUsernameIgnoreCase(request.email().trim(), request.username().trim()).isPresent()) {
            throw new EntityAlreadyExistsException("email", request.email(), User.class);
        }
        User requestUser = userMapper.toUser(request);
        User user = userRepository.save(requestUser);

        Role role = new Role();
        role.setUser(user);
        RoleId roleId = new RoleId();
        roleId.setRoleName(Roles.HUNTER.name());
        roleId.setUserId(user.getId());
        role.setId(roleId);

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        return userMapper.toUserAuthResponse(user);
    }


    @Transactional
    public User validateRefreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException(refreshToken, "Invalid refresh token"));

        if (!jwtService.validateToken(refreshToken)) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new RefreshTokenException(refreshToken, "Refresh token has expired. Please login again.");
        }

        return user;
    }

    @Transactional
    public void invalidateRefreshTokenByEmail(String email) {
        userRepository.invalidateRefreshTokenByEmail(email);
    }

    @Transactional
    public void updatePassword(Long id, ChangePasswordRequest changePasswordRequest) {
        User user = unwrapUser(userRepository.findById(id), id);

        if (!passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        user.setUpdatedAt(Instant.now());
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    public User getUser(Long id) {
        return unwrapUser(userRepository.findById(id), id);
    }

    public void updateUserPicture(Long id, MultipartFile image) throws IOException {
        if (!ALLOWED_TYPES.contains(image.getContentType())) {
            throw new BadRequestException("Only +" + String.join(" and ", ALLOWED_TYPES) + " images are allowed.");
        }

        User user = unwrapUser(userRepository.findById(id), id);
        String fileExtension = Objects.equals(image.getContentType(), "image/png") ? ".png" : ".jpg";
        String imageName = id + fileExtension;
        FIleUploadUtil.saveFile(USER_UPLOAD_DIR, imageName, image);
        user.setProfilePicture(imageName);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private User unwrapUser(Optional<User> optionalUser, Long id) {
        if (optionalUser.isPresent())
            return optionalUser.get();
        else
            throw new EntityNotFoundException(id, User.class);
    }

    public String getProfilePic(Long id) {
        return userRepository.getUserProfilePicById(id);
    }
}
