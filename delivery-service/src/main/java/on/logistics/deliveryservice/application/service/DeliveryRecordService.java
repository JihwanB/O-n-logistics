package on.logistics.deliveryservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordStatusRequestDto;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.GetDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.SearchDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordStatusResponse;

public interface DeliveryRecordService {

    CreateDeliveryRecordResponse createDeliveryRecord(CreateDeliveryRecordRequestDto requestDto);

    UpdateDeliveryRecordResponse updateActualDeliveryRecord(
        UpdateDeliveryRecordRequestDto requestDto);

    void deleteDeliveryRecord(UUID id, HttpServletRequest httpServletRequest);

    UpdateDeliveryRecordStatusResponse updateStatusDeliveryRecord(
        UpdateDeliveryRecordStatusRequestDto requestDto);

    UpdateDeliveryRecordStatusResponse updateStatusApiDeliveryRecord(
        UpdateDeliveryRecordStatusRequestDto requestDto);

    GetDeliveryRecordResponse getDeliveryRecord(UUID id, HttpServletRequest httpServletRequest);

    PageDto<SearchDeliveryRecordResponse> searchDeliveryRecord(
        SearchDeliveryRecordRequestDto requestDto);

    CreateDeliveryRecordResponse createApiDeliveryRecord(CreateDeliveryRecordRequestDto requestDto);
}
