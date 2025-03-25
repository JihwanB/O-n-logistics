package on.logistics.deliveryservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateAssignManagerRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRequestDto;
import on.logistics.deliveryservice.domain.entity.Delivery;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
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

public interface DeliveryService {

    CreateDeliveryResponse createDelivery(CreateDeliveryRequestDto requestDto);

    PageDto<SearchDeliveryResponse> searchDelivery(SearchDeliveryRequestDto requestDto);

    GetDeliveryResponse getDelivery(UUID id, HttpServletRequest httpServletRequest);

    UpdateDeliveryResponse updateDelivery(UpdateDeliveryRequestDto requestDto);

    void deleteDelivery(UUID id, HttpServletRequest httpServletRequest);

    UpdateAssignManagerResponse updateAssignManager(
        UpdateAssignManagerRequestDto updateAssignManagerRequestDto);

    UpdateDeliveryStatusHubMovingResponse updateDeliveryStatusHubMoving(UUID id,
        HttpServletRequest httpServletRequest);

    UpdateDeliveryStatusHubArriveResponse updateDeliveryStatusHubArrive(UUID id,
        HttpServletRequest httpServletRequest);

    UpdateDeliveryStatusCompanyMovingResponse updateDeliveryStatusCompanyMoving(UUID id,
        HttpServletRequest httpServletRequest);

    UpdateDeliveryStatusCompanyArriveResponse updateDeliveryStatusCompanyArrive(UUID id,
        HttpServletRequest httpServletRequest);

    UpdateDeliveryStatusCancelResponse updateDeliveryStatusCancel(UUID id,
        HttpServletRequest httpServletRequest);

    Delivery getOrElseThrow(UUID deliveryId);

    CreateDeliveryResponse createApiDelivery(CreateDeliveryRequestDto requestDto);

    void rollbackDeleteDelivery(UUID id);
    void createHubTransitRouteRequest(CreateDeliveryResponse response);
}
