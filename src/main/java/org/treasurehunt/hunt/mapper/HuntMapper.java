package org.treasurehunt.hunt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.treasurehunt.hunt.api.DraftHuntDTO;
import org.treasurehunt.hunt.api.LocationDTO;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.Location;

import java.util.List;

@Mapper(componentModel = "spring" )
public abstract class HuntMapper {

    @Mapping(source = "organizer.id", target = "organizerId" )
    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "status", target = "huntStatus" )
    public abstract DraftHuntDTO toDraftDTO(Hunt draftedHunt);

    public abstract List<DraftHuntDTO> toDraftDTOs(List<Hunt> allDrafted);

    LocationDTO map(Location location) {
        return new LocationDTO(location.getLatitude(), location.getLongitude());
    }
}
