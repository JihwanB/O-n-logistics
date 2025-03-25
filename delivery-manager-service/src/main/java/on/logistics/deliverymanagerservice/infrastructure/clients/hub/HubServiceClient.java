package on.logistics.deliverymanagerservice.infrastructure.clients.hub;

import java.util.UUID;
import on.logistics.deliverymanagerservice.infrastructure.clients.hub.feign.dtos.response.GetHubIdByUserId;
import org.springframework.stereotype.Service;

@Service
public interface HubServiceClient {

    GetHubIdByUserId getHubIdByUserId(UUID id);
}
