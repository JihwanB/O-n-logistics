package on.logistics.hubservice.infrastructure.clients.delivery.feign.dtos.response;

public record GetDeliveryRecordResponse(
    String deliveryId,
    String deliveryRecordId,
    int deliveryRecordSequence,
    String deliveryRecordStatus,
    String deliveryRecordStartHubId,
    String deliveryRecordEndHubId,
    String userId
) {

}
