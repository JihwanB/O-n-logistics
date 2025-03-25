package on.logistics.hubservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import on.logistics.hubservice.domain.entity.HubManager;
import org.springframework.stereotype.Repository;

@Repository
public interface HubManagerRepository {

    HubManager save(HubManager hubManager);

    Optional<HubManager> findByUserIdAndHubId(UUID userId, UUID hubId);

    Optional<HubManager> findByHubId(UUID hubId);

    Optional<HubManager> findByUserId(UUID userId);
}
