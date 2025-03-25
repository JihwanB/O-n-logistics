package on.logistics.hubservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.hubservice.domain.entity.HubManager;
import on.logistics.hubservice.infrastructure.persistence.jpa.HubManagerJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubManagerRepositoryImpl implements HubManagerRepository {

    private final HubManagerJpaRepository hubManagerJpaRepository;

    @Override
    public HubManager save(HubManager hubManager) {
        return hubManagerJpaRepository.save(hubManager);
    }

    @Override
    public Optional<HubManager> findByUserIdAndHubId(UUID userId, UUID hubId) {
        return hubManagerJpaRepository.findByUserIdAndHubId(userId, hubId);
    }

    @Override
    public Optional<HubManager> findByHubId(UUID hubId) {
        return hubManagerJpaRepository.findByHubId(hubId);
    }

    @Override
    public Optional<HubManager> findByUserId(UUID userId) {
        return hubManagerJpaRepository.findByUserId(userId);
    }
}
