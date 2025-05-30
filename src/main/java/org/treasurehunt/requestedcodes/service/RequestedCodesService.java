package org.treasurehunt.requestedcodes.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.common.util.UniqueKeyGenerator;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.requestedcodes.repo.RequestedCode;
import org.treasurehunt.requestedcodes.repo.RequestedCodesRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RequestedCodesService {

    private final RequestedCodesRepo requestedCodesRepo;
    private final HuntRepository huntRepository;
    private final UserRepository userRepository;

    public RequestedCodesService(RequestedCodesRepo requestedCodesRepo, HuntRepository huntRepository, UserRepository userRepository) {
        this.requestedCodesRepo = requestedCodesRepo;
        this.huntRepository = huntRepository;
        this.userRepository = userRepository;
    }

    /**
     * returns a random code of characters and numbers.
     *
     * @param huntId the hunt to generate the code for
     * @return the random code
     */
    public String requestCode(long huntId) {

        RequestedCode requestedCode = requestedCodesRepo.findByHuntId(huntId)
                .orElse(new RequestedCode());

        requestedCode.setHunt_id(huntId);
        requestedCode.setCode(UniqueKeyGenerator.generateKey());

        return requestedCodesRepo.save(requestedCode).getCode();
    }


    /**
     * returns the fetched code for the hunt.
     *
     * @param huntId huntId to fetch the code for.
     * @return the code for the hunt by hunt id
     */
    public String getCode(long huntId) {
        return requestedCodesRepo.findByHuntId(huntId)
                .orElseThrow(() -> new RuntimeException("Code not found for hunt ID " + huntId))
                .getCode();
    }

    @Transactional
    public Map<String, Object> submitCode(long huntId, @NotBlank String code) {
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();

        RequestedCode requestedCode = requestedCodesRepo.findByHuntId(huntId)
                .orElse(null);
        if (requestedCode == null) {
            objectObjectHashMap.put("success", false);
            return objectObjectHashMap;
        }

        boolean isEqualCodes = code.equals(requestedCode.getCode());

        if (isEqualCodes) {
            Hunt hunt = huntRepository.findById(huntId)
                    .orElseThrow(() -> new EntityNotFoundException("hunt not found"));

            var securityUser = AuthUtil.getUserFromSecurityContext();
            if (securityUser.isEmpty()) {
                throw new RuntimeException("No user available");
            }

            User user = userRepository.findById(securityUser.get().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            hunt.setWinner(user);

            huntRepository.save(hunt);
            requestedCode.setConsumed(true);
            requestedCodesRepo.save(requestedCode);
        }

        objectObjectHashMap.put("success", isEqualCodes);
        return objectObjectHashMap;
    }
}
