package on.logistics.deliveryservice.presentation.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRequestDto;
import on.logistics.deliveryservice.application.service.DeliveryService;
import on.logistics.deliveryservice.global.presentation.dtos.CommonResponse;
import on.logistics.deliveryservice.presentation.dtos.request.CreateDeliveryRequest;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/endpoint")
public class DeliveryEndpoint {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateDeliveryResponse>> createApiDelivery(
        @Valid @RequestBody CreateDeliveryRequest createDeliveryRequest,
        HttpServletRequest httpServletRequest) {
        final CreateDeliveryRequestDto requestDto = CreateDeliveryRequestDto.from(
            createDeliveryRequest, httpServletRequest);
        CreateDeliveryResponse response = deliveryService.createApiDelivery(requestDto);
        deliveryService.createHubTransitRouteRequest(response);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
