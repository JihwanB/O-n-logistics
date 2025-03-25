package on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.request;

import lombok.Builder;

@Builder
public record UpdateDeliveryStatusRequest(
    String deliveryRecordStatus
) {

    public static UpdateDeliveryStatusRequest of() {
        return UpdateDeliveryStatusRequest.builder()
            .deliveryRecordStatus("HUB_MOVING")
            .build();
    }
}
