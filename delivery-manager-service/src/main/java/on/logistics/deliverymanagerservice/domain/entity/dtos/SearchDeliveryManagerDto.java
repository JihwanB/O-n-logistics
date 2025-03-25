package on.logistics.deliverymanagerservice.domain.entity.dtos;

import java.util.UUID;
import lombok.Builder;
import on.logistics.deliverymanagerservice.application.dtos.SearchDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.global.enums.AuthRole;
import org.springframework.data.domain.Pageable;

@Builder
public record SearchDeliveryManagerDto(
    String keyword,
    String hubType,
    String deliveryType,
    Pageable pageable,
    AuthRole authRole,
    UUID hubId
) {

    public static SearchDeliveryManagerDto of(SearchDeliveryManagerRequestDto dto,
        AuthRole authRole, UUID hubId) {
        return SearchDeliveryManagerDto.builder()
            .keyword(dto.keyword())
            .hubType(dto.hubType())
            .deliveryType(dto.deliveryType())
            .pageable(dto.pageable())
            .authRole(authRole)
            .hubId(hubId)
            .build();
    }
}
