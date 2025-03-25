package on.logistics.deliverymanagerservice.infrastructure.clients.hub;

import feign.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliverymanagerservice.global.util.FeignClientResponseUtils;
import on.logistics.deliverymanagerservice.infrastructure.clients.hub.feign.HubServiceFeignClient;
import on.logistics.deliverymanagerservice.infrastructure.clients.hub.feign.dtos.response.GetHubIdByUserId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubServiceClientImpl implements HubServiceClient {

    private final HubServiceFeignClient hubServiceFeignClient;

    @Override
    public GetHubIdByUserId getHubIdByUserId(UUID userId) {
        Response response = hubServiceFeignClient.getHubIdByUserId(userId);
        return FeignClientResponseUtils.getBody(response, GetHubIdByUserId.class);
    }
}
