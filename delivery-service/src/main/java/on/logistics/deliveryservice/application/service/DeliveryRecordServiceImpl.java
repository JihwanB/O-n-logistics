package on.logistics.deliveryservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordStatusRequestDto;
import on.logistics.deliveryservice.domain.dtos.CreateDeliveryRecordDto;
import on.logistics.deliveryservice.domain.entity.Delivery;
import on.logistics.deliveryservice.domain.entity.DeliveryRecord;
import on.logistics.deliveryservice.domain.enums.DeliveryRecordStatus;
import on.logistics.deliveryservice.domain.enums.DeliveryStatus;
import on.logistics.deliveryservice.domain.repository.DeliveryRecordRepository;
import on.logistics.deliveryservice.exception.DeliveryException;
import on.logistics.deliveryservice.exception.DeliveryExceptionCode;
import on.logistics.deliveryservice.exception.DeliveryRecordException;
import on.logistics.deliveryservice.exception.DeliveryRecordExceptionCode;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
import on.logistics.deliveryservice.global.domain.Passport;
import on.logistics.deliveryservice.global.enums.AuthRole;
import on.logistics.deliveryservice.global.utils.PassportUtil;
import on.logistics.deliveryservice.infrastructure.clients.hub.HubServiceClient;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetHubInfo;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetHubManagerBooleanResponse;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.HubManagerBooleanRequest;
import on.logistics.deliveryservice.infrastructure.clients.map.MapServiceClient;
import on.logistics.deliveryservice.infrastructure.clients.map.feign.dtos.GetEstimateInfo;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.GetDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.SearchDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryRecordServiceImpl implements DeliveryRecordService {

    private final DeliveryRecordRepository deliveryRecordRepository;
    private final DeliveryService deliveryService;
    private final MapServiceClient mapServiceClient;
    private final HubServiceClient hubServiceClient;
    private final PassportUtil passportUtil;

    @Override
    @Transactional
    public CreateDeliveryRecordResponse createDeliveryRecord(
        CreateDeliveryRecordRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validMaster(passport);
        Delivery delivery = deliveryService.getOrElseThrow(requestDto.deliveryId());
        CreateDeliveryRecordDto createEntityDto = getCreateDeliveryRecordDto(requestDto);
        DeliveryRecord saved = DeliveryRecord.create(createEntityDto, delivery);
        deliveryRecordRepository.save(saved);
        return CreateDeliveryRecordResponse.of(saved.getId());
    }

    @Override
    @Transactional
    public CreateDeliveryRecordResponse createApiDeliveryRecord(
        CreateDeliveryRecordRequestDto requestDto) {
        Delivery delivery = deliveryService.getOrElseThrow(requestDto.deliveryId());
        CreateDeliveryRecordDto createEntityDto = getCreateDeliveryRecordDto(requestDto);
        DeliveryRecord saved = DeliveryRecord.create(createEntityDto, delivery);
        deliveryRecordRepository.save(saved);
        return CreateDeliveryRecordResponse.of(saved.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryRecordResponse updateActualDeliveryRecord(
        UpdateDeliveryRecordRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validCompanyManager(passport);
        DeliveryRecord deliveryRecord = getOrElseThrow(requestDto.deliveryRecordId());
        validHubManagerHubAndDeliveryManager(passport, deliveryRecord);
        deliveryRecord.update(requestDto.actualDistance(), requestDto.actualDuration());
        return UpdateDeliveryRecordResponse.of(deliveryRecord.getId());
    }

    @Override
    @Transactional
    public void deleteDeliveryRecord(UUID id, HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validMaster(passport);
        DeliveryRecord deliveryRecord = getOrElseThrow(id);
        deliveryRecord.deleteSoftly();
    }

    @Override
    @Transactional
    public UpdateDeliveryRecordStatusResponse updateStatusDeliveryRecord(
        UpdateDeliveryRecordStatusRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validCompanyManager(passport);
        DeliveryRecord deliveryRecord = getOrElseThrow(requestDto.deliveryRecordId());
        validHubManagerHubAndDeliveryManager(passport, deliveryRecord);
        if (deliveryRecord.getDelivery().getStatus() == DeliveryStatus.CANCEL) {
            throw new DeliveryRecordException(
                DeliveryRecordExceptionCode.DELIVERY_RECORD_DELIVERY_STATUS_CANCEL);
        }
        deliveryRecord.updateStatus(requestDto.status());
        return UpdateDeliveryRecordStatusResponse.of(deliveryRecord.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryRecordStatusResponse updateStatusApiDeliveryRecord(
        UpdateDeliveryRecordStatusRequestDto requestDto) {
        DeliveryRecord deliveryRecord = getOrElseThrow(requestDto.deliveryRecordId());
        if (deliveryRecord.getDelivery().getStatus() == DeliveryStatus.CANCEL) {
            throw new DeliveryRecordException(
                DeliveryRecordExceptionCode.DELIVERY_RECORD_DELIVERY_STATUS_CANCEL);
        }
        deliveryRecord.updateStatus(requestDto.status());
        return UpdateDeliveryRecordStatusResponse.of(deliveryRecord.getId());
    }

    @Override
    public GetDeliveryRecordResponse getDeliveryRecord(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        DeliveryRecord deliveryRecord = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, deliveryRecord);
        return GetDeliveryRecordResponse.from(deliveryRecord);
    }

    @Override
    public PageDto<SearchDeliveryRecordResponse> searchDeliveryRecord(
        SearchDeliveryRecordRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validCompanyManager(passport);
        Page<DeliveryRecord> deliveryRecordPage = deliveryRecordRepository.searchDeliveryRecord(
            requestDto);
        Page<SearchDeliveryRecordResponse> responsePage = deliveryRecordPage.map(
            SearchDeliveryRecordResponse::from);
        return PageDto.from(responsePage);
    }

    private DeliveryRecord getOrElseThrow(UUID deliveryRecordId) {
        return deliveryRecordRepository.findById(deliveryRecordId).orElseThrow(
            () -> new DeliveryRecordException(
                DeliveryRecordExceptionCode.DELIVERY_RECORD_NOT_FOUND));
    }

    private CreateDeliveryRecordDto getCreateDeliveryRecordDto(
        CreateDeliveryRecordRequestDto requestDto) {
        Long deliveryRecordCount = deliveryRecordRepository.countByDeliveryId(
            requestDto.deliveryId());

        long sequence = 1L;
        DeliveryRecordStatus deliveryRecordStatus = DeliveryRecordStatus.HUB_MOVING;

        if (deliveryRecordCount != 0) {
            sequence = deliveryRecordCount + 1;
            deliveryRecordStatus = DeliveryRecordStatus.HUB_WAITING;
        }

        UUID tmp = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (requestDto.deliveryRecordEndHubId().equals(tmp)) {
            return CreateDeliveryRecordDto.from(requestDto, sequence, deliveryRecordStatus);
        }

        GetEstimateInfo getEstimateInfo = getEstimateInfo(requestDto.deliveryRecordStartHubId(),
            requestDto.deliveryRecordEndHubId());
        return CreateDeliveryRecordDto.from(requestDto, sequence, deliveryRecordStatus,
            getEstimateInfo);
    }

    public GetEstimateInfo getEstimateInfo(UUID startHubId, UUID endHubId) {

        GetHubInfo startHubInfo = hubServiceClient.getHubInfo(startHubId);
        GetHubInfo endHubInfo = hubServiceClient.getHubInfo(endHubId);
        String start = "" + startHubInfo.longitude() + "," + startHubInfo.latitude();
        String end = "" + endHubInfo.longitude() + "," + endHubInfo.latitude();

        return mapServiceClient.getEstimate(start, end);
    }

    private Passport getPassport(HttpServletRequest passportRequest) {
        return passportUtil.getPassportByHttpServletRequest(passportRequest);
    }

    private void validMaster(Passport passport) {
        if (!passport.getRole().equals(AuthRole.MASTER.name())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validCompanyManager(Passport passport) {
        if (passport.getRole().equals(AuthRole.COMPANY_MANAGER.name())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validHubManagerHubAndDeliveryManager(Passport passport,
        DeliveryRecord deliveryRecord) {
        validHubManagerHub(passport, deliveryRecord);
        validDeliveryManager(passport, deliveryRecord);
    }

    private void validDeliveryManager(Passport passport, DeliveryRecord deliveryRecord) {
        if (passport.getRole().equals(AuthRole.DELIVERY_MANAGER.name())
            && !deliveryRecord.getUserId()
            .equals(passport.getUserId())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validHubManagerHub(Passport passport, DeliveryRecord deliveryRecord) {
        if (passport.getRole().equals(AuthRole.HUB_MANAGER.name())) {
            GetHubManagerBooleanResponse startHubManager = getHubManagerBooleanResponse(passport,
                deliveryRecord.getStartHubId());
            GetHubManagerBooleanResponse endHubManager = getHubManagerBooleanResponse(passport,
                deliveryRecord.getEndHubId());
            if (Boolean.FALSE.equals(startHubManager.isExist()) && Boolean.FALSE.equals(
                endHubManager.isExist())) {
                throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
            }
        }
    }

    private GetHubManagerBooleanResponse getHubManagerBooleanResponse(Passport passport,
        UUID hubId) {
        HubManagerBooleanRequest hubManagerBooleanRequest = HubManagerBooleanRequest.of(
            passport.getUserId(), hubId);
        return hubServiceClient.getHubManagerBoolean(hubManagerBooleanRequest);
    }

}
