package on.logistics.deliveryservice.presentation.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.deliveryservice.application.dtos.request.CreateDeliveryRecordRequestDto;
import on.logistics.deliveryservice.application.dtos.request.UpdateDeliveryRecordStatusRequestDto;
import on.logistics.deliveryservice.application.service.DeliveryRecordService;
import on.logistics.deliveryservice.global.exception.PassportException;
import on.logistics.deliveryservice.global.exception.PassportExceptionCode;
import on.logistics.deliveryservice.global.presentation.dtos.CommonResponse;
import on.logistics.deliveryservice.global.utils.PassportUtil;
import on.logistics.deliveryservice.presentation.dtos.request.CreateDeliveryRecordRequest;
import on.logistics.deliveryservice.presentation.dtos.request.UpdateDeliveryRecordStatusRequest;
import on.logistics.deliveryservice.presentation.dtos.response.CreateDeliveryRecordResponse;
import on.logistics.deliveryservice.presentation.dtos.response.UpdateDeliveryRecordStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/record/endpoint")
public class DeliveryRecordEndpoint {

    private final DeliveryRecordService deliveryRecordService;
    private final PassportUtil passportUtil;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateDeliveryRecordResponse>> createApiDeliveryRecord(
        @Valid @RequestBody CreateDeliveryRecordRequest createDeliveryRecordRequest,
        HttpServletRequest httpServletRequest) {
        String passportId = httpServletRequest.getHeader("X-Passport-Id");
        if (passportId == null) {
            throw new PassportException(PassportExceptionCode.PASSPORT_VALIDATION_FAILED);
        }
        CreateDeliveryRecordRequestDto requestDto = CreateDeliveryRecordRequestDto.from(
            createDeliveryRecordRequest, httpServletRequest);
        CreateDeliveryRecordResponse response = deliveryRecordService.createApiDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<CommonResponse<UpdateDeliveryRecordStatusResponse>> updateDeliveryRecordStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDeliveryRecordStatusRequest updateStatusDeliveryRecordRequest,
        HttpServletRequest httpServletRequest) {
        String passportId = httpServletRequest.getHeader("X-Passport-Id");
        if (passportId == null) {
            throw new PassportException(PassportExceptionCode.PASSPORT_VALIDATION_FAILED);
        }
        UpdateDeliveryRecordStatusRequestDto requestDto = UpdateDeliveryRecordStatusRequestDto.of(
            id, updateStatusDeliveryRecordRequest, httpServletRequest);
        UpdateDeliveryRecordStatusResponse response = deliveryRecordService.updateStatusApiDeliveryRecord(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

}
