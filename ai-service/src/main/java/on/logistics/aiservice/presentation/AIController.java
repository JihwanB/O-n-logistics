package on.logistics.aiservice.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import on.logistics.aiservice.application.service.AIService;
import on.logistics.aiservice.application.service.dtos.GenerateShippingDeadlineRequestDto;
import on.logistics.aiservice.global.presentation.dtos.CommonResponse;
import on.logistics.aiservice.infrastructure.clients.dtos.ShippingDeadlineResponseDto;
import on.logistics.aiservice.presentation.dtos.GenerateShippingDeadlineRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;

    @PostMapping("/shipping-deadline")
    public ResponseEntity<CommonResponse<ShippingDeadlineResponseDto>> generateShippingDeadline(
        @RequestBody @Valid GenerateShippingDeadlineRequest request
    ) {
        var requestDto = GenerateShippingDeadlineRequestDto.from(request);
        var responseDto = aiService.generateShippingDeadline(requestDto);
        log.info("responseDto: {}", responseDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }
}
