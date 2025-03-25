package on.logistics.hubservice.infrastructure.clients.delivery;

import feign.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.hubservice.global.util.FeignClientResponseUtils;
import on.logistics.hubservice.infrastructure.clients.delivery.feign.DeliveryServiceFeignClient;
import on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.request.UpdateDeliveryStatusRequest;
import on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.response.GetDeliveryRecordPageResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryServiceClientImpl implements DeliveryServiceClient {

    private final DeliveryServiceFeignClient deliveryServiceFeignClient;

    @Override
    public void updateDeliveryStatus(UUID id) {
        final var requestBody = UpdateDeliveryStatusRequest.of();
        deliveryServiceFeignClient.updateDeliveryStatus(id, requestBody);
    }

    @Override
    public GetDeliveryRecordPageResponse getDeliveryRecordId(String deliveryId, String startHubId) {
        Response response = deliveryServiceFeignClient.getDeliveryRecordId(deliveryId, startHubId);
        return FeignClientResponseUtils.getBody(response, GetDeliveryRecordPageResponse.class);
    }
}
