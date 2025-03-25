package on.logistics.hubtransitservice.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.hubtransitservice.domain.entity.HubTransit;
import on.logistics.hubtransitservice.domain.repository.HubTransitRepository;
import on.logistics.hubtransitservice.infrastructure.jpa.HubTransitJpaRepository;
import on.logistics.hubtransitservice.infrastructure.querydsl.HubTransitQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubTransitRepositoryImpl implements HubTransitRepository {

    private final HubTransitJpaRepository hubTransitJpaRepository;
    private final HubTransitQueryRepository hubTransitQueryRepository;

    @Override
    public HubTransit save(HubTransit hubTransit) {
        return hubTransitJpaRepository.save(hubTransit);
    }

    @Override
    public Optional<HubTransit> findById(UUID transitId) {
        return hubTransitJpaRepository.findById(transitId);
    }

    @Override
    public Optional<HubTransit> findByDeliveryId(UUID deliveryId) {
        return hubTransitJpaRepository.findByDeliveryId(deliveryId);
    }

    @Override
    public Optional<HubTransit> findByDeliveryIdAndNextHubId(
        UUID deliveryId,
        UUID hubId
    ) {
        return hubTransitJpaRepository.findByDeliveryIdAndNextHubId(deliveryId, hubId);
    }

    @Override
    public Page<HubTransit> searchHubTransit(String keyword, Pageable pageable
    ) {
        return hubTransitQueryRepository.searchHubTransit(keyword, pageable);
    }

}
