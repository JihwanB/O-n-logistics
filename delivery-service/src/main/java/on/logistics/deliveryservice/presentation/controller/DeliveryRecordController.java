package on.logistics.deliveryservice.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordStatusRequestDto;
import on.logistics.deliveryservice.application.service.DeliveryRecordService;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
import on.logistics.deliveryservice.global.presentation.dtos.CommonResponse;
import on.logistics.deliveryservice.presentation.dtos.request.CreateDeliveryRecordRequest;
import on.logistics.deliveryservice.presentation.dtos.request.UpdateDeliveryRecordRequest;
import on.logistics.deliveryservice.presentation.dtos.request.UpdateDeliveryRecordStatusRequest;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.GetDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.SearchDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordStatusResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/record")
public class DeliveryRecordController {

    private final DeliveryRecordService deliveryRecordService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateDeliveryRecordResponse>> createDeliveryRecord(
        @Valid @RequestBody CreateDeliveryRecordRequest createDeliveryRecordRequest,
        HttpServletRequest httpServletRequest) {
        CreateDeliveryRecordRequestDto requestDto = CreateDeliveryRecordRequestDto.from(
            createDeliveryRecordRequest, httpServletRequest);
        CreateDeliveryRecordResponse response = deliveryRecordService.createDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageDto<SearchDeliveryRecordResponse>>> searchDeliveryRecord(
        @RequestParam(required = false) UUID deliveryId,
        @RequestParam(required = false) UUID startHubId,
        @RequestParam(required = false) UUID endHubId, @RequestParam(required = false) UUID userId,
        @PageableDefault Pageable pageable, HttpServletRequest httpServletRequest) {
        SearchDeliveryRecordRequestDto requestDto = SearchDeliveryRecordRequestDto.from(deliveryId,
            startHubId, endHubId, userId, pageable, httpServletRequest);
        PageDto<SearchDeliveryRecordResponse> response = deliveryRecordService.searchDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));

    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetDeliveryRecordResponse>> getDeliveryRecord(
        @PathVariable UUID id, HttpServletRequest httpServletRequest) {
        GetDeliveryRecordResponse response = deliveryRecordService.getDeliveryRecord(id,
            httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/actual/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryRecordResponse>> updateActualDeliveryRecord(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDeliveryRecordRequest updateDeliveryRecordRequest,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryRecordRequestDto requestDto = UpdateDeliveryRecordRequestDto.from(id,
            updateDeliveryRecordRequest, httpServletRequest);
        UpdateDeliveryRecordResponse response = deliveryRecordService.updateActualDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDeliveryRecord(@PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        deliveryRecordService.deleteDeliveryRecord(id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryRecordStatusResponse>> updateDeliveryRecordStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDeliveryRecordStatusRequest updateStatusDeliveryRecordRequest,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryRecordStatusRequestDto requestDto = UpdateDeliveryRecordStatusRequestDto.of(
            id, updateStatusDeliveryRecordRequest, httpServletRequest);
        UpdateDeliveryRecordStatusResponse response = deliveryRecordService.updateStatusDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
