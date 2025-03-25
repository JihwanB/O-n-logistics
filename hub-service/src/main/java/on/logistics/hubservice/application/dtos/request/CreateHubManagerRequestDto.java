package on.logistics.hubservice.application.dtos.request;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.Builder;
import on.logistics.hubservice.presentation.dtos.request.CreateHubManagerRequest;

@Builder
public record CreateHubManagerRequestDto(
    UUID userId,
    UUID hubId,
    HttpServletRequest passportRequest
) {

    public static CreateHubManagerRequestDto of(CreateHubManagerRequest request,
        HttpServletRequest passportRequest) {
        return CreateHubManagerRequestDto.builder()
            .userId(UUID.fromString(request.userId()))
            .hubId(UUID.fromString(request.hubId()))
            .passportRequest(passportRequest)
            .build();
    }
}
