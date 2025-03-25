package on.logistics.hubservice.infrastructure.clients.delivery;

import java.util.UUID;
import on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.response.GetDeliveryRecordPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface DeliveryServiceClient {

    void updateDeliveryStatus(UUID id);

    GetDeliveryRecordPageResponse getDeliveryRecordId(@RequestParam String deliveryId,
        @RequestParam String startHubId);
}
