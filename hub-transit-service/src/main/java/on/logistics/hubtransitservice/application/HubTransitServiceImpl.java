package on.logistics.hubtransitservice.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.hubtransitservice.application.dtos.request.CreateHubTransitRequestDto;
import on.logistics.hubtransitservice.application.dtos.request.GetNextHubRequestDto;
import on.logistics.hubtransitservice.application.dtos.request.InboundHubTransitRequestDto;
import on.logistics.hubtransitservice.application.dtos.request.UpdateHubTransitRequestDto;
import on.logistics.hubtransitservice.application.dtos.response.CreateHubTransitResponseDto;
import on.logistics.hubtransitservice.domain.dtos.CreateHubTransitDto;
import on.logistics.hubtransitservice.domain.entity.HubTransit;
import on.logistics.hubtransitservice.domain.entity.Route;
import on.logistics.hubtransitservice.domain.enums.DeliveryType;
import on.logistics.hubtransitservice.domain.repository.HubTransitRepository;
import on.logistics.hubtransitservice.domain.repository.RouteRepository;
import on.logistics.hubtransitservice.exception.HubTransitException;
import on.logistics.hubtransitservice.exception.HubTransitExceptionCode;
import on.logistics.hubtransitservice.infrastructure.clients.deliverymanager.DeliveryManagerClient;
import on.logistics.hubtransitservice.infrastructure.clients.deliverymanager.feign.dtos.AssignDeliveryManagerRequest;
import on.logistics.hubtransitservice.infrastructure.clients.deliverymanager.feign.dtos.AssignDeliveryManagerResponse;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.DeliveryServiceClient;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign.dtos.CreateDeliveryRecordRequest;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign.dtos.CreateDeliveryRecordResponse;
import on.logistics.hubtransitservice.infrastructure.clients.deliveryservice.feign.dtos.UpdateDeliveryStatusRequest;
import on.logistics.hubtransitservice.infrastructure.clients.hub.HubServiceClient;
import on.logistics.hubtransitservice.infrastructure.clients.hub.feign.dtos.GetHubResponse;
import on.logistics.hubtransitservice.presentation.dtos.response.CreateHubTransitResponse;
import on.logistics.hubtransitservice.presentation.dtos.response.GetHubTransitResponse;
import on.logistics.hubtransitservice.presentation.dtos.response.GetNextHubResponse;
import on.logistics.hubtransitservice.presentation.dtos.response.SearchHubTransitResponse;
import on.logistics.hubtransitservice.presentation.dtos.response.UpdateHubTransitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubTransitServiceImpl implements HubTransitService {

    private final HubTransitRepository hubTransitRepository;
    private final RouteRepository routeRepository;
    private final HubServiceClient hubServiceClient;
    private final DeliveryServiceClient deliveryServiceClient;
    private final DeliveryManagerClient deliveryManagerClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CreateHubTransitResponse createHubTransit(CreateHubTransitRequestDto requestDto) {
        log.info("허브 이동정보 생성 요청");

        Optional<HubTransit> existingTransit = hubTransitRepository
            .findByDeliveryId(requestDto.deliveryId());
        if (existingTransit.isPresent()) {
            log.info("이미 존재하는 허브 이동정보로 인해 추가적으로 생성하지 않고 기존 레코드 반환, deliveryId: {}",
                requestDto.deliveryId());
            return CreateHubTransitResponseDto.from(existingTransit.get());
        }

        var startHub = getHubInfo(requestDto.startHubId());
        var endHub = getHubInfo(requestDto.endHubId());

        var route = getRouteByHubNames(startHub.hubName(), endHub.hubName());
        String routeSnapshot = route.getPathJson();

        var currentHubId = startHub.id();
        var currentHubName = startHub.hubName();

        String nextHubName = determineNextHubName(routeSnapshot, currentHubName);
        UUID nextHubId = getNextHubIdByName(nextHubName);
        DeliveryType deliveryType = getNextDeliveryType(nextHubName);

        var deliveryManager = getDeliveryManager(
            requestDto.deliveryId(), currentHubId, deliveryType);

        var deliveryRecord = createDeliveryRecord(
            requestDto.deliveryId(), currentHubId, nextHubId, deliveryManager);

        CreateHubTransitDto createDto = CreateHubTransitDto.builder()
            .deliveryId(requestDto.deliveryId())
            .deliveryRecordId(deliveryRecord.deliveryRecordId())
            .currentHubId(currentHubId)
            .currentHubName(currentHubName)
            .nextHubId(nextHubId)
            .nextHubName(nextHubName)
            .nextDeliveryType(deliveryType)
            .userId(deliveryManager.userId())
            .routeSnapshot(routeSnapshot)
            .build();

        HubTransit hubTransit = HubTransit.create(createDto);
        HubTransit saved = hubTransitRepository.save(hubTransit);
        log.info("허브 이동정보 생성 완료, id: {}", saved.getId());
        return CreateHubTransitResponseDto.from(saved);
    }

    @Override
    @Transactional
    public void processInboundHubTransit(
        InboundHubTransitRequestDto requestDto
    ) {
        log.info("허브 입고 요청, deliveryId: {}, currentHubId: {}", requestDto.deliveryId(),
            requestDto.currentHubId());

        Optional<HubTransit> existingTransit = hubTransitRepository
            .findByDeliveryIdAndNextHubId(requestDto.deliveryId(), requestDto.currentHubId());
        if (existingTransit.isPresent()) {
            log.info("이미 처리된 허브 입고 요청, deliveryId: {}, currentHubId: {}",
                requestDto.deliveryId(), requestDto.currentHubId()
            );
            return;
        }

        HubTransit currentTransit = hubTransitRepository
            .findByDeliveryIdAndNextHubId(requestDto.deliveryId(), requestDto.currentHubId())
            .orElseThrow(
                () -> new HubTransitException(HubTransitExceptionCode.HUB_TRANSIT_NOT_FOUND));

        if (currentTransit.getNextHubName().getValue().equals("END_OF_HUB")) {
            throw new HubTransitException(HubTransitExceptionCode.NO_FURTHER_HUB);
        }

        String routeSnapshot = currentTransit.getRouteSnapshot();
        String newNextHubName = determineNextHubName(
            routeSnapshot,
            currentTransit.getNextHubName().getValue()
        );

        UUID newNextHubId = getNextHubIdByName(newNextHubName);
        DeliveryType newNextDeliveryType = getNextDeliveryType(newNextHubName);

        UUID newCurrentHubId = currentTransit.getNextHubId();
        String newCurrentHubName = currentTransit.getNextHubName().getValue();

        var deliveryManager = getDeliveryManager(
            requestDto.deliveryId(), newCurrentHubId, newNextDeliveryType);

        var deliveryRecord = createDeliveryRecord(
            requestDto.deliveryId(), newCurrentHubId, newNextHubId, deliveryManager);

        var updateDeliveryRecordRequest = UpdateDeliveryStatusRequest.builder()
            .deliveryRecordStatus("HUB_ARRIVE").build();
        deliveryServiceClient.updateDeliveryRecordStatus(
            currentTransit.getDeliveryRecordId(), updateDeliveryRecordRequest);

        CreateHubTransitDto nextTransitDto = CreateHubTransitDto.builder()
            .deliveryId(requestDto.deliveryId())
            .deliveryRecordId(deliveryRecord.deliveryRecordId())
            .currentHubId(newCurrentHubId)
            .currentHubName(newCurrentHubName)
            .nextHubId(newNextHubId)
            .nextHubName(newNextHubName)
            .nextDeliveryType(newNextDeliveryType)
            .userId(deliveryManager.userId())
            .routeSnapshot(routeSnapshot)
            .build();

        HubTransit nextHubTransit = HubTransit.create(nextTransitDto);
        HubTransit saved = hubTransitRepository.save(nextHubTransit);
        log.info("허브 입고 요청 처리 완료, new transitId: {}", saved.getId());
    }

    @Override
    public GetHubTransitResponse getHubTransit(UUID transitId) {
        HubTransit hubTransit = getOrElseThrow(transitId);
        log.info("허브간 이동정보 조회 성공, transitId: {}", transitId);
        return GetHubTransitResponse.from(hubTransit);
    }

    @Override
    public Page<SearchHubTransitResponse> searchHubTransit(String keyword, Pageable pageable) {
        log.info("허브간 이동정보 조회, keyword={}", keyword);
        return hubTransitRepository.searchHubTransit(keyword, pageable)
            .map(SearchHubTransitResponse::from);
    }

    @Override
    public GetNextHubResponse getNextHubTransit(GetNextHubRequestDto requestDto) {
        HubTransit hubTransit = getOrElseThrow(requestDto.transitId());
        log.info("다음 허브 조회 성공, transitId: {}", requestDto.transitId());
        return GetNextHubResponse.from(hubTransit);
    }

    @Override
    @Transactional
    public UpdateHubTransitResponse updateHubTransit(UpdateHubTransitRequestDto requestDto) {
        log.info("배송 담당자 업데이트 요청, dto: {}", requestDto);
        HubTransit hubTransit = getOrElseThrow(requestDto.transitId());
        hubTransit.updateDeliveryManagerId(requestDto.userId());
        log.info("배송 담당자 업데이트 완료, transitId: {}", hubTransit.getId());
        return UpdateHubTransitResponse.from(hubTransit);
    }

    @Override
    @Transactional
    public void deleteHubTransit(UUID transitId) {
        log.info("허브 이동정보 삭제 요청, transitId: {}", transitId);
        HubTransit hubTransit = getOrElseThrow(transitId);
        hubTransit.deleteSoftly();
        log.info("허브 이동정보 soft delted, transitId: {}", transitId);
    }

    private HubTransit getOrElseThrow(UUID routeId) {
        return hubTransitRepository.findById(routeId)
            .orElseThrow(
                () -> new HubTransitException(HubTransitExceptionCode.HUB_TRANSIT_NOT_FOUND));
    }

    private GetHubResponse getHubInfo(UUID hubId) {
        var hubResponse = hubServiceClient.getHubById(hubId);
        if (hubResponse == null) {
            throw new HubTransitException(HubTransitExceptionCode.HUB_NOT_FOUND);
        }
        return hubResponse;
    }

    private Route getRouteByHubNames(String startHubName, String endHubName) {
        return routeRepository.findByStartHubNameAndEndHubName(startHubName, endHubName)
            .orElseThrow(() -> new HubTransitException(HubTransitExceptionCode.ROUTE_NOT_FOUND));
    }

    private String determineNextHubName(String pathJson, String currentHubName) {
        try {
            List<String> routePath = objectMapper.readValue(pathJson, new TypeReference<>() {
            });
            int currentIndex = routePath.indexOf(currentHubName);
            if (currentIndex == -1) {
                throw new HubTransitException(HubTransitExceptionCode.ROUTE_INVALID);
            }
            if (currentIndex < routePath.size() - 1) {
                return routePath.get(currentIndex + 1);
            } else {
                return "END_OF_HUB";
            }
        } catch (Exception e) {
            throw new HubTransitException(HubTransitExceptionCode.ROUTE_JSON_PARSE_ERROR);
        }
    }

    private UUID getNextHubIdByName(String nextHubName) {
        if ("END_OF_HUB".equals(nextHubName)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        var hubResponse = hubServiceClient.getHubByName(nextHubName);
        if (hubResponse == null) {
            throw new HubTransitException(HubTransitExceptionCode.HUB_NOT_FOUND);
        }
        return hubResponse.id();
    }

    private DeliveryType getNextDeliveryType(String nextHubName) {
        return "END_OF_HUB".equals(nextHubName) ? DeliveryType.COMPANY_DELIVERY
            : DeliveryType.HUB_DELIVERY;
    }

    private AssignDeliveryManagerResponse getDeliveryManager(
        UUID deliveryId, UUID currentHubId, DeliveryType deliveryType
    ) {
        return deliveryManagerClient.assignDeliveryManager(
            AssignDeliveryManagerRequest.builder()
                .deliveryId(deliveryId)
                .hubId(currentHubId)
                .deliveryType(String.valueOf(deliveryType))
                .build()
        );
    }

    private CreateDeliveryRecordResponse createDeliveryRecord(
        UUID deliveryId, UUID currentHubId, UUID nextHubId,
        AssignDeliveryManagerResponse deliveryManager
    ) {
        return deliveryServiceClient.createDeliveryRecord(
            CreateDeliveryRecordRequest.builder()
                .deliveryId(deliveryId)
                .deliveryRecordStartHubId(currentHubId)
                .deliveryRecordEndHubId(nextHubId)
                .userId(deliveryManager.userId())
                .build()
        );
    }

}
