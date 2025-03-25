package on.logistics.orderservice.infrastructure.clients.delivery.feign;

import feign.Response;
import on.logistics.orderservice.infrastructure.clients.delivery.feign.dtos.DeliveryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service")
public interface DeliveryServiceFeignClient {

    @PostMapping("/api/v1/delivery/endpoint")
    Response deliveryRequest(@RequestBody DeliveryRequest request);
}
