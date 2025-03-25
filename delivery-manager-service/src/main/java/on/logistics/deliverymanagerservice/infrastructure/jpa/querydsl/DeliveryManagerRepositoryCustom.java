package on.logistics.deliverymanagerservice.infrastructure.jpa.querydsl;

import java.util.Optional;
import java.util.UUID;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryManager;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryType;
import on.logistics.deliverymanagerservice.domain.entity.dtos.SearchDeliveryManagerDto;
import on.logistics.deliverymanagerservice.global.application.dtos.PageDto;
import on.logistics.deliverymanagerservice.presentation.dtos.response.SearchDeliveryManagerResponse;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryManagerRepositoryCustom {

    Integer findMaxSequenceByHubIdAndType(UUID hubId, DeliveryType deliveryType);

    Optional<DeliveryManager> findLastAssignedManager(UUID hubId, DeliveryType type);

    Optional<DeliveryManager> findNextDeliveryManager(UUID hubId, Integer sequence);

    Optional<DeliveryManager> findFirstByHubIdOrderBySequenceAsc(UUID hubId, DeliveryType type);

    PageDto<SearchDeliveryManagerResponse> searchDeliveryManager(
        SearchDeliveryManagerDto requestDto);
}
