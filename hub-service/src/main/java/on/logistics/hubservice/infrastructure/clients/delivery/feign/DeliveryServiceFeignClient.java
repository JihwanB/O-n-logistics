package on.logistics.hubservice.infrastructure.clients.delivery.feign;

import feign.Response;
import java.util.UUID;
import on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.request.UpdateDeliveryStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "delivery-service")
public interface DeliveryServiceFeignClient {

    @PutMapping("/api/v1/delivery/record/status/{id}")
    Response updateDeliveryStatus(@PathVariable UUID id,
        @RequestBody UpdateDeliveryStatusRequest request);

    @GetMapping("/api/v1/delivery/record/search")
    Response getDeliveryRecordId(@RequestParam String deliveryId, @RequestParam String startHubId);
}
