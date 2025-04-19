package org.treasurehunt.hunt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.hunt.api.CreateHuntRequest;
import org.treasurehunt.hunt.api.DraftHuntDTO;
import org.treasurehunt.hunt.api.HuntFilter;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.HuntSpecification;
import org.treasurehunt.hunt.repository.LocationRepository;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.Location;
import org.treasurehunt.user.repository.entity.User;
import org.treasurehunt.user.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.treasurehunt.common.constants.UploadingConstants.*;

@Service
@RequiredArgsConstructor
public class HuntService {

    private final HuntRepository huntRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final ValidatorService validatorService;
    private final ObjectMapper objectMapper;

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
                throw new BadRequestException("Only +" + String.join(" and ", ALLOWED_TYPES) + " images are allowed." );
            }

            Location savedLocation = locationRepository.save(createHuntRequest.getLocation());
            Hunt hunt = Hunt.builder()
                    .organizer(organizer)
                    .title(createHuntRequest.getTitle())
                    .description(createHuntRequest.getDescription())
                    .startDate(createHuntRequest.getStartDate())
                    .endDate(createHuntRequest.getEndDate())
                    .location(savedLocation)
                    .status(HuntStatus.DRAFT)
                    .build();

            // save hunt as draft in DB
            Hunt savedHunt = huntRepository.save(hunt);

            String bgExtension = Objects.equals(bgImage.getContentType(), "image/png" ) ? ".png" : ".jpg";
            String mapExtension = Objects.equals(mapImage.getContentType(), "image/png" ) ? ".png" : ".jpg";
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
     * @param filter filtering criteria
     * @return page of hunts matching the criteria
     */
    public Page<Hunt> getAllHunts(Pageable pageable, HuntFilter filter) {
        if (filter == null) {
            filter = new HuntFilter();
        }
        return huntRepository.findAll(HuntSpecification.getSpecification(filter), pageable);
    }
}
