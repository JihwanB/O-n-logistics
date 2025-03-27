package on.logistics.orderservice.infrastructure.clients.delivery.dtos;

import java.util.UUID;
import on.logistics.orderservice.domain.entity.VendorOrder;

public record RollbackDeliveryRequestDto(
    UUID orderId
) {

    public static RollbackDeliveryRequestDto from(VendorOrder vendorOrder) {
        return new RollbackDeliveryRequestDto(
            vendorOrder.getId()
        );
    }
}
