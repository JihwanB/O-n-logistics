package on.logistics.hubservice.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.hubservice.application.dtos.request.RetrievalLogisticsRequestDto;
import on.logistics.hubservice.application.dtos.request.StorageLogisticsRequestDto;
import on.logistics.hubservice.domain.entity.HubLogisticsRecord;
import on.logistics.hubservice.domain.repository.HubLogisticsRecordRepository;
import on.logistics.hubservice.domain.repository.HubRepository;
import on.logistics.hubservice.exception.HubException;
import on.logistics.hubservice.exception.HubExceptionCode;
import on.logistics.hubservice.infrastructure.clients.delivery.DeliveryServiceClient;
import on.logistics.hubservice.infrastructure.clients.hubtransit.HubTransitServiceClient;
import on.logistics.hubservice.infrastructure.clients.hubtransit.feign.dtos.request.NextHubTransitRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubLogisticsRecordService {

    private final HubLogisticsRecordRepository hubLogisticsRecordRepository;
    private final HubRepository hubRepository;
    private final HubTransitServiceClient hubTransitServiceClient;
    private final DeliveryServiceClient deliveryServiceClient;

    @Transactional
    public void storage(StorageLogisticsRequestDto requestDto) {
        validateHubExists(requestDto.hubId());

        List<HubLogisticsRecord> records = new ArrayList<>();
        requestDto.storageLogisticsIds().forEach(id -> {
            final var record = HubLogisticsRecord.storage(requestDto.hubId(), id);
            records.add(record);

            final var nextHubTransitRequest = NextHubTransitRequest.of(id, requestDto.hubId());
            hubTransitServiceClient.requestNextHubTransit(nextHubTransitRequest);
        });
        hubLogisticsRecordRepository.saveAll(records);
    }

    public void retrieval(RetrievalLogisticsRequestDto requestDto) {
        validateHubExists(requestDto.hubId());
        List<HubLogisticsRecord> records = hubLogisticsRecordRepository.findAllByDeliveryIdIn(
            requestDto.retrievalLogisticsIds());
        records.forEach(HubLogisticsRecord::retrieval);
        records.stream()
            .map(HubLogisticsRecord::getDeliveryId)
            .forEach(deliveryId -> {
                final var deliveryRecordId = deliveryServiceClient.getDeliveryRecordId(
                        deliveryId.toString(), requestDto.hubId().toString()).content().get(0)
                    .deliveryRecordId();
                deliveryServiceClient.updateDeliveryStatus(
                    UUID.fromString(deliveryRecordId));
            });
        hubLogisticsRecordRepository.saveAll(records);
    }

    private void validateHubExists(UUID hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new HubException(HubExceptionCode.HUB_NOT_FOUND);
        }
    }
}
