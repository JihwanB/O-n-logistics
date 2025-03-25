package on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign;

import feign.Response;
import java.util.UUID;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign.dtos.CreateDeliveryRecordRequest;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign.dtos.UpdateDeliveryStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service")
public interface DeliveryServiceFeignClient {

    @PostMapping("/api/v1/delivery/record/endpoint")
    Response createDeliveryRecord(@RequestBody CreateDeliveryRecordRequest request);

    @PutMapping("/api/v1/delivery/record/endpoint/status/{id}")
    Response updateDeliveryRecordStatus(
        @PathVariable("id") UUID id,
        @RequestBody UpdateDeliveryStatusRequest request
    );

}
