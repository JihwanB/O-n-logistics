package on.logistics.hubservice.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import on.logistics.hubservice.domain.entity.HubManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubManagerJpaRepository extends JpaRepository<HubManager, UUID> {

    Optional<HubManager> findByUserIdAndHubId(UUID userId, UUID hubId);

    Optional<HubManager> findByHubId(UUID hubId);

    Optional<HubManager> findByUserId(UUID userId);
}
