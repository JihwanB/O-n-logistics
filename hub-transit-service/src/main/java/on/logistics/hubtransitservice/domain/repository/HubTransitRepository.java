package on.logistics.hubtransitservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import on.logistics.hubtransitservice.domain.entity.HubTransit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubTransitRepository {

    HubTransit save(HubTransit hubTransit);

    Optional<HubTransit> findById(UUID transitId);

    Optional<HubTransit> findByDeliveryId(UUID deliveryId);

    Optional<HubTransit> findByDeliveryIdAndNextHubId(UUID deliveryId, UUID hubId);

    Page<HubTransit> searchHubTransit(String keyword, Pageable pageable);

}
