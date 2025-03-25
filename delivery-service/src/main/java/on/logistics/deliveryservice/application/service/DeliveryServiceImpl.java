package on.logistics.deliveryservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.deliveryservice.application.dtos.DeliveryHubInfoDto;
import on.logistics.deliveryservice.application.dtos.DeliveryUserInfoDto;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateAssignManagerRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRequestDto;
import on.logistics.deliveryservice.domain.dtos.CreateDeliveryDto;
import on.logistics.deliveryservice.domain.entity.Delivery;
import on.logistics.deliveryservice.domain.enums.DeliveryStatus;
import on.logistics.deliveryservice.domain.repository.DeliveryRepository;
import on.logistics.deliveryservice.exception.DeliveryException;
import on.logistics.deliveryservice.exception.DeliveryExceptionCode;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
import on.logistics.deliveryservice.global.domain.Passport;
import on.logistics.deliveryservice.global.enums.AuthRole;
import on.logistics.deliveryservice.global.utils.PassportUtil;
import on.logistics.deliveryservice.infrastructure.clients.exception.ExternalApiException;
import on.logistics.deliveryservice.infrastructure.clients.hub.HubServiceClient;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetHubInfo;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetHubManagerBooleanResponse;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetMiddleHubPageInfo;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.GetSpokeHubInfo;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.HubInfo;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.HubManagerBooleanRequest;
import on.logistics.deliveryservice.infrastructure.clients.hub.feign.dtos.HubType;
import on.logistics.deliveryservice.infrastructure.clients.hubTransit.HubTransitServiceClient;
import on.logistics.deliveryservice.infrastructure.clients.hubTransit.feign.dtos.CreateHubTransitRouteRequest;
import on.logistics.deliveryservice.infrastructure.clients.map.MapServiceClient;
import on.logistics.deliveryservice.infrastructure.clients.map.feign.dtos.GetDestinationInfo;
import on.logistics.deliveryservice.infrastructure.clients.map.feign.dtos.GetHubRouteInfo;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryResponse;
import on.logistics.deliveryservice.presentation.dtos.response.GetDeliveryResponse;
import on.logistics.deliveryservice.presentation.dtos.response.SearchDeliveryResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateAssignManagerResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryStatusCancelResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryStatusCompanyArriveResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryStatusCompanyMovingResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryStatusHubArriveResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryStatusHubMovingResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "DeliveryServiceImpl")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MapServiceClient mapServiceClient;
    private final HubServiceClient hubServiceClient;
    private final HubTransitServiceClient hubTransitServiceClient;
    private final PassportUtil passportUtil;

    @Override
    @Transactional
    public CreateDeliveryResponse createDelivery(CreateDeliveryRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        if (!passport.getRole().equals(AuthRole.MASTER.name())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }

        GetHubInfo startHubInfo = hubServiceClient.getHubInfo(requestDto.startHubId());
        if (startHubInfo == null) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_START_HUB_NOT_FOUND);
        }

        DeliveryHubInfoDto hubInfo = deliveryHubInfo(requestDto.destination());
        DeliveryUserInfoDto userInfo = deliveryUserInfo(passport);
        CreateDeliveryDto entityRequestDto = CreateDeliveryDto.from(requestDto, hubInfo, userInfo);
        Delivery saved = Delivery.create(entityRequestDto);
        deliveryRepository.save(saved);
        return CreateDeliveryResponse.of(saved.getId());
    }

    // 권한 해제 임시 분리
    @Transactional
    public CreateDeliveryResponse createApiDelivery(CreateDeliveryRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());

        GetHubInfo startHubInfo = hubServiceClient.getHubInfo(requestDto.startHubId());
        if (startHubInfo == null) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_START_HUB_NOT_FOUND);
        }

        DeliveryHubInfoDto hubInfo = deliveryHubInfo(requestDto.destination());
        DeliveryUserInfoDto userInfo = deliveryUserInfo(passport);
        CreateDeliveryDto entityRequestDto = CreateDeliveryDto.from(requestDto, hubInfo, userInfo);
        Delivery saved = Delivery.create(entityRequestDto);
        deliveryRepository.save(saved);
        return CreateDeliveryResponse.of(saved.getId());
    }

    @Override
    public PageDto<SearchDeliveryResponse> searchDelivery(SearchDeliveryRequestDto requestDto) {
        Page<Delivery> deliveryPage = deliveryRepository.searchDelivery(requestDto);
        Page<SearchDeliveryResponse> responsePage = deliveryPage.map(SearchDeliveryResponse::from);
        return PageDto.from(responsePage);
    }

    @Override
    public GetDeliveryResponse getDelivery(UUID id, HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        Delivery delivery = getOrElseThrow(id);
        validDeliveryManager(passport, delivery);
        return GetDeliveryResponse.from(delivery);
    }

    @Override
    @Transactional
    public UpdateDeliveryResponse updateDelivery(UpdateDeliveryRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validCompanyManager(passport);

        Delivery delivery = getOrElseThrow(requestDto.deliveryId());
        if (!delivery.getStatus().equals(DeliveryStatus.HUB_WAITING)) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_START);
        }
        validHubManagerHubAndDeliveryManager(passport, delivery);
        log.info(requestDto.toString());
        DeliveryHubInfoDto hubInfo = deliveryHubInfo(requestDto.destination());
        delivery.update(requestDto.destination(), hubInfo);
        return UpdateDeliveryResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public void deleteDelivery(UUID id, HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyMangerAndDeliveryManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHub(passport, delivery);
        delivery.deleteSoftly();
    }

    @Override
    @Transactional
    public UpdateAssignManagerResponse updateAssignManager(
        UpdateAssignManagerRequestDto updateAssignManagerRequestDto) {
        Passport passport = getPassport(updateAssignManagerRequestDto.httpServletRequest());
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(updateAssignManagerRequestDto.deliveryId());
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateAssignManager(updateAssignManagerRequestDto.userId());
        return UpdateAssignManagerResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryStatusHubMovingResponse updateDeliveryStatusHubMoving(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateDeliveryStatusHubMoving();
        return UpdateDeliveryStatusHubMovingResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryStatusHubArriveResponse updateDeliveryStatusHubArrive(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateDeliveryStatusHubArrive();
        return UpdateDeliveryStatusHubArriveResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryStatusCompanyMovingResponse updateDeliveryStatusCompanyMoving(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateDeliveryStatusCompanyMoving();
        return UpdateDeliveryStatusCompanyMovingResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryStatusCompanyArriveResponse updateDeliveryStatusCompanyArrive(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateDeliveryStatusCompanyArrive();
        return UpdateDeliveryStatusCompanyArriveResponse.of(delivery.getId());
    }

    @Override
    @Transactional
    public UpdateDeliveryStatusCancelResponse updateDeliveryStatusCancel(UUID id,
        HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        validCompanyManager(passport);
        Delivery delivery = getOrElseThrow(id);
        validHubManagerHubAndDeliveryManager(passport, delivery);
        delivery.updateDeliveryStatusCancel();
        return UpdateDeliveryStatusCancelResponse.of(delivery.getId());
    }

    public DeliveryHubInfoDto deliveryHubInfo(String destination) {
        // todo: 목적지 위도, 경도 받아옴.
        log.info("delivery hub info: {}", destination);
        GetDestinationInfo geocode = mapServiceClient.getGeocode(destination);
        log.info("geocode: {}", geocode.toString());
        String start = "" + geocode.longitude() + "" + "," + geocode.latitude();
        GetMiddleHubPageInfo getMiddleHubPageInfo = typeHubInfoList();
        GetHubRouteInfo middleRoute = middleRouteInfo(start, getMiddleHubPageInfo);
        UUID middleRouteHubId = middleRouteHubId(getMiddleHubPageInfo, middleRoute);
        GetSpokeHubInfo getSpokeHubInfo = typeSpokeInfoList(middleRouteHubId);
        GetHubRouteInfo endRoute = endRouteInfo(start, getSpokeHubInfo);
        UUID endHubId = endRouteHubId(getSpokeHubInfo, endRoute);

        return DeliveryHubInfoDto.of(endHubId);
    }

    private UUID endRouteHubId(GetSpokeHubInfo getSpokeHubInfo, GetHubRouteInfo endRoute) {
        List<HubInfo> hubs = getSpokeHubInfo.data();
        String spiltEndRouteHubLongitude = String.valueOf(
            endRoute.summary().end().location().get(0)).substring(0, 6);
        String spiltEndRouteHubLatitude = String.valueOf(endRoute.summary().end().location().get(1))
            .substring(0, 6);
        String endHubId = "";
        for (HubInfo typeHubInfo : hubs) {
            String splitHubLongitude = typeHubInfo.longitude().substring(0, 6);
            String splitHubLatitude = typeHubInfo.latitude().substring(0, 6);
            if (splitHubLongitude.equals(spiltEndRouteHubLongitude) && splitHubLatitude.equals(
                spiltEndRouteHubLatitude)) {
                endHubId = typeHubInfo.id();
                break;
            }
        }
        return UUID.fromString(endHubId);
    }

    private GetHubRouteInfo endRouteInfo(String start, GetSpokeHubInfo getSpokeHubInfo) {
        List<HubInfo> hubs = getSpokeHubInfo.data();
        String end = getMapApiEndSerchingString(hubs);
        return mapServiceClient.getRoute(start, end);
    }

    public GetHubRouteInfo middleRouteInfo(String start,
        GetMiddleHubPageInfo getMiddleHubPageInfo) {
        List<HubInfo> hubs = getMiddleHubPageInfo.content();
        String end = getMapApiEndSerchingString(hubs);
        return mapServiceClient.getRoute(start, end);
    }

    public UUID middleRouteHubId(GetMiddleHubPageInfo getMiddleHubPageInfo,
        GetHubRouteInfo middleRoute) {
        List<HubInfo> hubs = getMiddleHubPageInfo.content();
        String spiltMiddleRouteHubLongitude = String.valueOf(
            middleRoute.summary().end().location().get(0)).substring(0, 6);
        String spiltMiddleRouteHubLatitude = String.valueOf(
            middleRoute.summary().end().location().get(1)).substring(0, 6);
        String middleRouteId = "";
        for (HubInfo typeHubInfo : hubs) {
            String splitHubLongitude = typeHubInfo.longitude().substring(0, 6);
            String splitHubLatitude = typeHubInfo.latitude().substring(0, 6);
            if (splitHubLongitude.equals(spiltMiddleRouteHubLongitude) && splitHubLatitude.equals(
                spiltMiddleRouteHubLatitude)) {
                middleRouteId = typeHubInfo.id();
                break;
            }
        }
        return UUID.fromString(middleRouteId);
    }

    public GetMiddleHubPageInfo typeHubInfoList() {
        return hubServiceClient.searchHubs(HubType.HUB);
    }

    public GetSpokeHubInfo typeSpokeInfoList(UUID middleHubId) {
        return hubServiceClient.getSpokeHubInfo(middleHubId);
    }

    public DeliveryUserInfoDto deliveryUserInfo(Passport passport) {
        String recipient = passport.getNickname();
        String recipientSlackEmail = passport.getSlackEmail();
        return DeliveryUserInfoDto.of(recipient, recipientSlackEmail);
    }

    private String getMapApiEndSerchingString(List<HubInfo> hubs) {
        String end = "";
        for (HubInfo typeHubInfo : hubs) {
            end += ("" + typeHubInfo.longitude() + "," + typeHubInfo.latitude() + ":");
        }
        if (end.endsWith(":")) {
            end = end.substring(0, end.length() - 1);
        }
        return end;
    }

    @Override
    public void createHubTransitRouteRequest(CreateDeliveryResponse response) {
        Delivery delivery = getOrElseThrow(response.deliveryId());
        CreateHubTransitRouteRequest createHubTransitRouteRequest = CreateHubTransitRouteRequest.of(
            delivery.getStartHubId(), delivery.getEndHubId(), delivery.getId());
        try {
            hubTransitServiceClient.createHubTransitRoute(createHubTransitRouteRequest);
        } catch (Exception e) {
            delivery.deleteSoftly();
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_HUB_TRANSIT_ERROR);
        }
    }

    public Delivery getOrElseThrow(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new DeliveryException(DeliveryExceptionCode.DELIVERY_NOT_FOUND));
    }

    private Passport getPassport(HttpServletRequest passportRequest) {
        return passportUtil.getPassportByHttpServletRequest(passportRequest);
    }

    private void validCompanyManager(Passport passport) {
        if (passport.getRole().equals(AuthRole.COMPANY_MANAGER.name())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validCompanyMangerAndDeliveryManager(Passport passport) {
        if (passport.getRole().equals(AuthRole.COMPANY_MANAGER.name()) || passport.getRole()
            .equals(AuthRole.DELIVERY_MANAGER.name())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validHubManagerHubAndDeliveryManager(Passport passport, Delivery delivery) {
        validHubManagerHub(passport, delivery);
        validDeliveryManager(passport, delivery);
    }

    private void validDeliveryManager(Passport passport, Delivery delivery) {
        if (passport.getRole().equals(AuthRole.DELIVERY_MANAGER.name()) && !delivery.getUserId()
            .equals(passport.getUserId())) {
            throw new DeliveryException(DeliveryExceptionCode.DELIVERY_ACCESS_DENIED);
        }
    }

    private void validHubManagerHub(Passport passport, Delivery delivery) {
        if (passport.getRole().equals(AuthRole.HUB_MANAGER.name())) {
            GetHubManagerBooleanResponse startHubManager = getHubManagerBooleanResponse(passport,
                delivery.getStartHubId());
            GetHubManagerBooleanResponse endHubManager = getHubManagerBooleanResponse(passport,
                delivery.getEndHubId());
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

    @Transactional
    public void rollbackDeleteDelivery(UUID deliveryId) {
        log.info("Delivery 롤백 요청");
        Delivery delivery = getOrElseThrow(deliveryId);
        deliveryRepository.delete(delivery);
    }

}
