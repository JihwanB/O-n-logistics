package on.logistics.deliverymanagerservice.domain.entity.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryManager;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryType;
import on.logistics.deliverymanagerservice.domain.entity.dtos.SearchDeliveryManagerDto;
import on.logistics.deliverymanagerservice.global.application.dtos.PageDto;
import on.logistics.deliverymanagerservice.infrastructure.jpa.DeliveryManagerJpaRepository;
import on.logistics.deliverymanagerservice.infrastructure.jpa.querydsl.DeliveryManagerRepositoryCustom;
import on.logistics.deliverymanagerservice.presentation.dtos.response.SearchDeliveryManagerResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryManagerRepositoryImpl implements DeliveryManagerRepository {

    private final DeliveryManagerJpaRepository deliveryManagerJpaRepository;
    private final DeliveryManagerRepositoryCustom deliveryManagerRepositoryCustom;

    @Override
    public DeliveryManager save(DeliveryManager deliveryManager) {
        return deliveryManagerJpaRepository.save(deliveryManager);
    }

    @Override
    public Integer findMaxSequenceByHubIdAndType(
        UUID hubId,
        DeliveryType deliveryType) {
        return deliveryManagerRepositoryCustom.findMaxSequenceByHubIdAndType(hubId,
            deliveryType);
    }

    @Override
    public Optional<DeliveryManager> findByIdAndIsDeleted(UUID id, boolean isDeleted) {
        return deliveryManagerJpaRepository.findByIdAndIsDeleted(id, isDeleted);
    }

    @Override
    public Optional<DeliveryManager> findLastAssignedManager(UUID hubId, DeliveryType type) {
        return deliveryManagerRepositoryCustom.findLastAssignedManager(hubId, type);
    }

    @Override
    public Optional<DeliveryManager> findNextDeliveryManager(UUID hubId, Integer sequence) {
        return deliveryManagerRepositoryCustom.findNextDeliveryManager(hubId, sequence);
    }

    @Override
    public Optional<DeliveryManager> findFirstByHubIdOrderBySequenceAsc(UUID hubId,
        DeliveryType type) {
        return deliveryManagerRepositoryCustom.findFirstByHubIdOrderBySequenceAsc(hubId, type);
    }

    @Override
    public Optional<DeliveryManager> findByIdAndUserId(UUID deliveryManagerId,
        UUID userId) {
        return deliveryManagerJpaRepository.findByIdAndUserId(deliveryManagerId, userId);
    }

    @Override
    public PageDto<SearchDeliveryManagerResponse> searchDeliveryManager(
        SearchDeliveryManagerDto requestDto) {
        return deliveryManagerRepositoryCustom.searchDeliveryManager(requestDto);
    }

    @Override
    public void delete(DeliveryManager deliveryManager) {
        deliveryManagerJpaRepository.delete(deliveryManager);
    }
}
