package org.treasurehunt.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.email.EmailService;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.DraftHuntDTO;
import org.treasurehunt.hunt.mapper.HuntMapper;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.service.HuntService;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewerService {
    private final HuntRepository huntRepository;
    private final HuntMapper huntMapper;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void submitHuntToReview(Long huntId, Long userId) {

        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Not hunt found for id" + huntId)
                );

        if (!Objects.equals(hunt.getOrganizer().getId(), userId)) {
            throw new AccessDeniedException("ACCESS DENIED");
        }

        if (hunt.getStatus() != HuntStatus.DRAFT) {
            throw new IllegalStateException();
        }

        hunt.setStatus(HuntStatus.UNDER_REVIEW);
        huntRepository.save(hunt);
    }

    public List<DraftHuntDTO> getReviewerHunts() {
        UserDetailsDTO user = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(
                        () -> new AccessDeniedException("ACCESS DEFINED")
                );

        List<Hunt> allByReviewerId = huntRepository.findAllByReviewer_Id(user.getId());
        return huntMapper.toDraftDTOs(allByReviewerId);
    }

    public void assignReviewer(Long huntId) {
        UserDetailsDTO userDetailsDTO = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find user")
                );
        User reviewer = userRepository.findById(userDetailsDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("No reviewer found"));

        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException("Not available hunt with id" + huntId));

        if (hunt.getReviewer() == null) {
            hunt.setReviewer(reviewer);
        } else {
            hunt.setReviewer(null);
        }
        huntRepository.save(hunt);
    }

    public void approveHunt(Long huntId) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException("Not available hunt with id" + huntId));

        hunt.setStatus(HuntStatus.APPROVED);
        huntRepository.save(hunt);

        // Notify the organizer of the approval
        emailService.sendSimpleEmail(
                hunt.getOrganizer().getEmail(),
                "Hunt Approval", "Congrats, your hunt is not approved.\n You can now set the time and publish it!!!");
    }
}
