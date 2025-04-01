package on.logistics.orderservice.infrastructure.clients.delivery;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.orderservice.application.service.DeliveryService;
import on.logistics.orderservice.global.utils.FeignClientResponseUtils;
import on.logistics.orderservice.infrastructure.clients.delivery.dtos.DeliveryRequestDto;
import on.logistics.orderservice.infrastructure.clients.delivery.dtos.RollbackDeliveryRequestDto;
import on.logistics.orderservice.infrastructure.clients.delivery.feign.DeliveryServiceFeignClient;
import on.logistics.orderservice.infrastructure.clients.delivery.feign.dtos.DeliveryRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryServiceFeignClient deliveryServiceFeignClient;

    @Override
    public void deliveryRequest(DeliveryRequestDto requestDto) {
        log.info("Delivery request: {}", requestDto);
        DeliveryRequest request = DeliveryRequest.from(requestDto);
        Response response = deliveryServiceFeignClient.deliveryRequest(request);
        FeignClientResponseUtils.validateResponseStatus(response);
    }

    @Override
    public void rollbackDeliveryRequest(RollbackDeliveryRequestDto requestDto) {
        log.info("Rollback delivery request: {}", requestDto);
        log.warn("배송 도메인에 공급 업체별 주문 id로 생성된 배송을 삭제하는 API가 없습니다!");
    }
}
