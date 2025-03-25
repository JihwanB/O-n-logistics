package on.logistics.hubservice.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.hubservice.application.dtos.GetHubManagerIdResponseDto;
import on.logistics.hubservice.application.dtos.request.CreateHubManagerRequestDto;
import on.logistics.hubservice.application.dtos.request.ValidHubManagerRequestDto;
import on.logistics.hubservice.application.service.HubManagerService;
import on.logistics.hubservice.global.presentation.dtos.CommonResponse;
import on.logistics.hubservice.presentation.dtos.request.CreateHubManagerRequest;
import on.logistics.hubservice.presentation.dtos.request.ValidHubManagerRequest;
import on.logistics.hubservice.presentation.dtos.response.CreateHubManagerResponse;
import on.logistics.hubservice.presentation.dtos.response.GetHubIdByUserIdResponseDto;
import on.logistics.hubservice.presentation.dtos.response.ValidHubManagerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hubs/manager")
public class HubManagerController {

    private final HubManagerService hubManagerService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateHubManagerResponse>> createHubManager(
        @RequestBody @Valid CreateHubManagerRequest createHubManagerRequest,
        HttpServletRequest passportRequest) {
        final var requestDto = CreateHubManagerRequestDto.of(createHubManagerRequest,
            passportRequest);
        final var responseDto = hubManagerService.createHubManager(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @PostMapping("/valid")
    public ResponseEntity<CommonResponse<ValidHubManagerResponse>> validHubManager(
        @RequestBody @Valid ValidHubManagerRequest validHubManagerRequest) {
        final var requestDto = ValidHubManagerRequestDto.of(validHubManagerRequest);
        final var responseDto = hubManagerService.validHubManager(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @GetMapping("/{hubId}/manager")
    public ResponseEntity<CommonResponse<GetHubManagerIdResponseDto>> getHubManagerId(
        @PathVariable UUID hubId
    ) {
        final var responseDto = hubManagerService.getHubManagerId(hubId);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<CommonResponse<GetHubIdByUserIdResponseDto>> getHubIdByUserId(
        @PathVariable UUID userId) {
        final var responseDto = hubManagerService.getHubIdByUserId(userId);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }
}
