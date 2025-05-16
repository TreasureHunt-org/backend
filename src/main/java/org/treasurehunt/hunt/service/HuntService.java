package org.treasurehunt.hunt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.*;
import org.treasurehunt.hunt.mapper.ChallengeMapperImpl;
import org.treasurehunt.hunt.mapper.HuntMapper;
import org.treasurehunt.hunt.repository.*;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Comment;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.Location;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;
import org.treasurehunt.user.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.treasurehunt.common.constants.UploadingConstants.*;

@Service
@RequiredArgsConstructor
public class HuntService {

    private final HuntRepository huntRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final ValidatorService validatorService;
    private final ObjectMapper objectMapper;
    private final HuntMapper huntMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepo submissionRepo;

    @Transactional
    public Hunt draftHunt(
            String rawCreateHuntRequest,
            MultipartFile bgImage,
            MultipartFile mapImage,
            Long organizerId) throws IOException {
        try {
            // Convert a JSON string to a Java object
            CreateHuntRequest createHuntRequest = objectMapper.readValue(rawCreateHuntRequest, CreateHuntRequest.class);
            // validate request
            validatorService.validate(createHuntRequest);

            User organizer = userService.getUser(organizerId);
            System.out.println(createHuntRequest);

            if (!ALLOWED_TYPES.contains(bgImage.getContentType()) || !ALLOWED_TYPES.contains(mapImage.getContentType())) {
                throw new BadRequestException("Only +" + String.join(" and ", ALLOWED_TYPES) + " images are allowed.");
            }

            Location savedLocation = locationRepository.save(createHuntRequest.getLocation());
            Hunt hunt = Hunt.builder()
                    .organizer(organizer)
                    .title(createHuntRequest.getTitle())
                    .description(createHuntRequest.getDescription())
//                    .startDate(createHuntRequest.getStartDate())
//                    .endDate(createHuntRequest.getEndDate())
                    .location(savedLocation)
                    .status(HuntStatus.DRAFT)
                    .build();

            // save hunt as draft in DB
            Hunt savedHunt = huntRepository.save(hunt);

            String bgExtension = Objects.equals(bgImage.getContentType(), "image/png") ? ".png" : ".jpg";
            String mapExtension = Objects.equals(mapImage.getContentType(), "image/png") ? ".png" : ".jpg";
            String bgName = savedHunt.getId() + bgExtension;
            String mapName = savedHunt.getId() + mapExtension;
            FIleUploadUtil.saveFile(HUNT_BG_UPLOAD_DIR, bgName, bgImage);
            FIleUploadUtil.saveFile(HUNT_MAP_UPLOAD_DIR, mapName, mapImage);

            // save images path to DB
            savedHunt.setHuntImgUri(bgName);
            savedHunt.setMapImgUri(mapName);

            return huntRepository.save(savedHunt);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Failed to process Hunt JSON: " + ex.getMessage());
        }
    }

    public List<Hunt> getAllDrafted() {
        return huntRepository.findAll();
    }

    /**
     * Get all hunts with filtering and pagination
     *
     * @param pageable pagination information
     * @param filter   filtering criteria
     * @return page of hunts matching the criteria
     */
    public Page<Hunt> getAllHunts(Pageable pageable, HuntFilter filter) {
        if (filter == null) {
            filter = new HuntFilter();
        }
        return huntRepository.findAll(HuntSpecification.getSpecification(filter), pageable);
    }

    public DraftHuntDTO getHunt(Long huntId) {
        UserDetailsDTO user = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(() -> new AccessDeniedException("ACCESS DENOTED"));

        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException("Didn't find hunt with id " + huntId));

        boolean isAdmin = false;
        for (GrantedAuthority authority : user.getAuthorities()) {
            if (List.of(Roles.ADMIN.name(), Roles.REVIEWER.name()).contains(authority.getAuthority())) {
                isAdmin = true;
                break;
            }
        }
        return huntMapper.toDraftDTO(hunt);
    }

    public String getBackgroundPic(Long id) {
        return huntRepository.getHuntBgImageById(id);
    }

    public String getMapPic(Long id) {
        return huntRepository.getHuntMapImageById(id);
    }

    public CommentResponse addComment(Long huntId, CommentRequest content) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Couldn't find hunt")
                );

        UserDetailsDTO user = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(
                        () -> new EntityNotFoundException("Couldn't find user")
                );
        boolean isAllowed = false;
        for (GrantedAuthority authority : user.getAuthorities()) {
            if (List.of(Roles.ADMIN.name(), Roles.REVIEWER.name()).contains(authority.getAuthority())) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new AccessDeniedException("ACCESS DENIED");
        }

        User reviewer = userRepository.findById(user.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Couldn't find user")
                );

        Comment comment = new Comment();
        comment.setHunt(hunt);
        comment.setContent(content.content());
        comment.setReviewer(reviewer);

        Comment save = commentRepository.save(comment);
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(save.getId());
        commentResponse.setHuntId(huntId);
        commentResponse.setReviewerId(reviewer.getId());
        commentResponse.setContent(comment.getContent());
        return commentResponse;
    }

    public List<CommentResponse> getAllComments(Long huntId) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Couldn't find hunt")
                );

        List<Comment> comments = commentRepository.findAllByHunt_Id(huntId);

        List<CommentResponse> responses = new ArrayList<>();

        comments.forEach(
                comment -> {
                    CommentResponse response = new CommentResponse();
                    response.setId(comment.getId());
                    response.setHuntId(huntId);
                    response.setReviewerId(hunt.getReviewer().getId());
                    response.setContent(comment.getContent());
                    responses.add(response);
                }
        );
        return responses;
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public void updateHunt(Long huntId, HuntUpdateRequest huntUpdateRequest) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Couldn't find hunt")
                );

        hunt.setStartDate(huntUpdateRequest.startDate());
        hunt.setEndDate(huntUpdateRequest.endDate());
        huntRepository.save(hunt);
    }

    public HuntStatistics getHuntStatistics(Long huntId) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException("Couldn't find hunt"));

        Long numOfChallenges = challengeRepository.countByHunt_Id(hunt.getId());
        Long numOfParticipants = huntRepository.countParticipants(hunt.getId());

        return new HuntStatistics(numOfChallenges, numOfParticipants);
    }

    public void joinHunt(Long huntId) {

        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException("Couldn't find hunt"));
        UserDetailsDTO u = AuthUtil.getUserFromSecurityContext().orElseThrow(() -> new EntityNotFoundException("Couldn't find hunt"));

        User user = userRepository.findById(u.getId()).orElseThrow(() -> new EntityNotFoundException("Couldn't find hunt"));

        if(user.getHunt() != null){
            throw new BadRequestException("User is already in a hunt");
        }

        user.setHunt(hunt);
        userRepository.save(user);
    }

    public ActiveHuntsResponse getActiveHunts() {
        // Get the current user
        Long userId = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(() -> new EntityNotFoundException("No user found"))
                .getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId, User.class));

        // Get the hunt associated with the user
        Hunt hunt = huntRepository.findHuntByUser_Id(userId);
        if (hunt == null || hunt.getStatus() != HuntStatus.LIVE) {
            // Return empty response if user has no active hunt
            return new ActiveHuntsResponse(new ArrayList<>());
        }

        // Get all challenges for the hunt
        List<Challenge> challenges = hunt.getChallenges();
        int totalChallenges = challenges.size();

        // Count completed challenges
        int completedChallenges = 0;
        for (Challenge challenge : challenges) {
            List<Submission> submissions = submissionRepo.findByChallengeIdAndUserId(challenge.getId(), userId);
            boolean solved = submissions.stream()
                    .anyMatch(s -> s.getStatus().equals(Submission.SubmissionStatus.SUCCESS));
            if (solved) {
                completedChallenges++;
            }
        }

        // Calculate progress
        double progress = totalChallenges > 0 ? (double) completedChallenges / totalChallenges * 100 : 0;

        // Convert endDate to LocalDate
        java.time.LocalDate dueDate = hunt.getEndDate() != null 
            ? hunt.getEndDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate() 
            : null;

        // Create active hunt item
        ActiveHuntsResponse.ActiveHuntItem item = ActiveHuntsResponse.ActiveHuntItem.builder()
                .id(hunt.getId().toString())
                .title(hunt.getTitle())
                .progress(progress)
                .completedChallenges(completedChallenges)
                .totalChallenges(totalChallenges)
                .dueDate(dueDate)
                .build();

        // Create and return response
        return new ActiveHuntsResponse(List.of(item));
    }

    public CompletedHuntsResponse getCompletedHunts() {
        // Get the current user
        Long userId = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(() -> new EntityNotFoundException("No user found"))
                .getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId, User.class));

        // Get the hunt associated with the user
        Hunt hunt = huntRepository.findHuntByUser_Id(userId);
        if (hunt == null || hunt.getStatus() != HuntStatus.FINISHED) {
            // Return empty response if user has no completed hunt
            return new CompletedHuntsResponse(new ArrayList<>());
        }

        // Convert endDate to LocalDate (as completedDate)
        java.time.LocalDate completedDate = hunt.getEndDate() != null 
            ? hunt.getEndDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate() 
            : null;

        // For this implementation, we'll use the user's score and a placeholder rank
        // In a real implementation, you would calculate the rank based on scores of all users
        int score = user.getScore() != null ? user.getScore() : 0;
        int rank = 1; // Placeholder, would need to be calculated based on all users' scores

        // Create completed hunt item
        CompletedHuntsResponse.CompletedHuntItem item = CompletedHuntsResponse.CompletedHuntItem.builder()
                .id(hunt.getId().toString())
                .title(hunt.getTitle())
                .completedDate(completedDate)
                .score(score)
                .rank(rank)
                .build();

        // Create and return response
        return new CompletedHuntsResponse(List.of(item));
    }
}
