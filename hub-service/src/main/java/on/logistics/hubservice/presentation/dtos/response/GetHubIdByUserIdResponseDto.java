package on.logistics.hubservice.presentation.dtos.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GetHubIdByUserIdResponseDto(
    String hubId
) {

    public static GetHubIdByUserIdResponseDto of(UUID hubId) {
        return GetHubIdByUserIdResponseDto.builder()
            .hubId(String.valueOf(hubId))
            .build();
    }
}
