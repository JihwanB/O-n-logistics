package on.logistics.deliveryservice.presentation.endpoint;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.deliveryservice.application.service.DeliveryService;
import on.logistics.deliveryservice.global.presentation.dtos.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/rollback")
public class DeliveryEndpoint {

    private final DeliveryService deliveryService;

    @PostMapping("/delete/{id}")
    ResponseEntity<CommonResponse<Void>> rollbackDelete(@PathVariable UUID id) {
        deliveryService.rollbackDeleteDelivery(id);
        return ResponseEntity.ok(CommonResponse.success());
    }

}
