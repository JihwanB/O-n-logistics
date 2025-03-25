package on.logistics.deliveryservice.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.SearchDeliveryRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateAssignManagerRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRequestDto;
import on.logistics.deliveryservice.application.service.DeliveryService;
import on.logistics.deliveryservice.domain.enums.DeliveryStatus;
import on.logistics.deliveryservice.global.application.dtos.PageDto;
import on.logistics.deliveryservice.global.presentation.dtos.CommonResponse;
import on.logistics.deliveryservice.presentation.dtos.request.CreateDeliveryRequest;
import on.logistics.deliveryservice.presentation.dtos.request.UpdateAssignManagerRequest;
import on.logistics.deliveryservice.presentation.dtos.request.UpdateDeliveryRequest;
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
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateDeliveryResponse>> createDelivery(
        @Valid @RequestBody CreateDeliveryRequest createDeliveryRequest,
        HttpServletRequest httpServletRequest) {
        final CreateDeliveryRequestDto requestDto = CreateDeliveryRequestDto.from(
            createDeliveryRequest, httpServletRequest);
        CreateDeliveryResponse response = deliveryService.createApiDelivery(requestDto);
        deliveryService.createHubTransitRouteRequest(response);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageDto<SearchDeliveryResponse>>> searchDelivery(
        @RequestParam(required = false) String destination,
        @RequestParam(required = false) String recipient,
        @RequestParam(required = false) DeliveryStatus status,
        @RequestParam(required = false) UUID companyDeliveryManagerId,
        @PageableDefault Pageable pageable,
        HttpServletRequest httpServletRequest) {
        SearchDeliveryRequestDto requestDto = SearchDeliveryRequestDto.from(destination, recipient,
            status, companyDeliveryManagerId, pageable, httpServletRequest);
        PageDto<SearchDeliveryResponse> response = deliveryService.searchDelivery(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetDeliveryResponse>> getDelivery(@PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        GetDeliveryResponse response = deliveryService.getDelivery(id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryResponse>> updateDelivery(
        @PathVariable UUID id, @Valid @RequestBody UpdateDeliveryRequest updateDeliveryRequest,
        HttpServletRequest httpServletRequest) {
        final UpdateDeliveryRequestDto requestDto = UpdateDeliveryRequestDto.from(id,
            updateDeliveryRequest, httpServletRequest);
        UpdateDeliveryResponse response = deliveryService.updateDelivery(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDelivery(@PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        deliveryService.deleteDelivery(id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @PutMapping("/assignManager/{id}")
    public ResponseEntity<CommonResponse<UpdateAssignManagerResponse>> updateAssignManager(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAssignManagerRequest updateAssignManagerRequest,
        HttpServletRequest httpServletRequest) {
        UpdateAssignManagerRequestDto requestDto = UpdateAssignManagerRequestDto.from(id,
            updateAssignManagerRequest, httpServletRequest);
        UpdateAssignManagerResponse response = deliveryService.updateAssignManager(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/hubMoving/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryStatusHubMovingResponse>> updateDeliveryStatusHubMoving(
        @PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryStatusHubMovingResponse response = deliveryService.updateDeliveryStatusHubMoving(
            id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/hubArrive/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryStatusHubArriveResponse>> updateDeliveryStatusHubArrive(
        @PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryStatusHubArriveResponse response = deliveryService.updateDeliveryStatusHubArrive(
            id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/companyMoving/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryStatusCompanyMovingResponse>> updateDeliveryStatusCompanyMoving(
        @PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryStatusCompanyMovingResponse response = deliveryService.updateDeliveryStatusCompanyMoving(
            id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/companyArrive/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryStatusCompanyArriveResponse>> updateDeliveryStatusCompanyArrive(
        @PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryStatusCompanyArriveResponse response = deliveryService.updateDeliveryStatusCompanyArrive(
            id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/cancel/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryStatusCancelResponse>> updateDeliveryStatusCancel(
        @PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        UpdateDeliveryStatusCancelResponse response = deliveryService.updateDeliveryStatusCancel(
            id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
