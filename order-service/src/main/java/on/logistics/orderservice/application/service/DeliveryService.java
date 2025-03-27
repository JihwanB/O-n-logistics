package on.logistics.orderservice.application.service;

import on.logistics.orderservice.infrastructure.clients.delivery.dtos.DeliveryRequestDto;
import on.logistics.orderservice.infrastructure.clients.delivery.dtos.RollbackDeliveryRequestDto;

public interface DeliveryService {

    void deliveryRequest(DeliveryRequestDto requestDto);

    void rollbackDeliveryRequest(RollbackDeliveryRequestDto requestDto);
}
