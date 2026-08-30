package it.unical.ea.Travel.Mappers.itinerary;

import it.unical.ea.Travel.Entities.itinerary.ItineraryJoinRequest;
import it.unical.ea.dtos.itinerary.ItineraryJoinRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ItineraryJoinRequestMapper {

    @Mapping(target = "itineraryId", source = "itinerary.id")
    @Mapping(target = "itineraryTitle", source = "itinerary.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(request.getUser() != null ? (request.getUser().getUserType() == it.unical.ea.enums.UserType.SOCIETA ? request.getUser().getCompanyName() : (request.getUser().getFirstName() + \" \" + request.getUser().getLastName()).trim()) : null)")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userAvatarUrl", source = "user.avatarUrl")
    ItineraryJoinRequestDto toDTO(ItineraryJoinRequest request);

    List<ItineraryJoinRequestDto> toDTOList(List<ItineraryJoinRequest> requests);
}
