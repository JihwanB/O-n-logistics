package on.logistics.slackservice.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.slackservice.application.SlackMessageService;
import on.logistics.slackservice.exception.SlackException;
import on.logistics.slackservice.exception.SlackExceptionCode;
import on.logistics.slackservice.global.domain.Passport;
import on.logistics.slackservice.global.presentation.dtos.CommonResponse;
import on.logistics.slackservice.global.utils.PassportUtil;
import on.logistics.slackservice.presentation.dtos.ReadSlackMessageResponse;
import on.logistics.slackservice.presentation.dtos.SlackMessageRequest;
import on.logistics.slackservice.presentation.dtos.SlackMessageResponse;
import on.logistics.slackservice.presentation.dtos.UpdateSlackMessageRequest;
import on.logistics.slackservice.presentation.dtos.UpdateSlackMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/slack")
@RequiredArgsConstructor
public class SlackMessageController {

    private final SlackMessageService slackMessageService;
    private final PassportUtil passportUtil;

    @PostMapping
    public ResponseEntity<CommonResponse<SlackMessageResponse>> sendMessage(
        @Valid @RequestBody SlackMessageRequest request,
        HttpServletRequest servletRequest
    ) {
        Passport passport = passportUtil.getPassportByHttpServletRequest(servletRequest);
        if (passport == null) {
            throw new SlackException(SlackExceptionCode.UNAUTHORIZED);
        }
        final var requestDto = SlackMessageRequest.from(request);
        final var responseDto = slackMessageService.sendSlackMessage(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ReadSlackMessageResponse>> getMessage(
        @PathVariable UUID id, HttpServletRequest servletRequest
    ) {
        Passport passport = passportUtil.getPassportByHttpServletRequest(servletRequest);
        if (passport == null) {
            throw new SlackException(SlackExceptionCode.UNAUTHORIZED);
        }
        final var responseDto = slackMessageService.getSlackMessage(id);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<UpdateSlackMessageResponse>> updateMessage(
        @PathVariable UUID id, @RequestBody UpdateSlackMessageRequest request,
        HttpServletRequest servletRequest
    ) {
        Passport passport = passportUtil.getPassportByHttpServletRequest(servletRequest);
        if (passport == null) {
            throw new SlackException(SlackExceptionCode.UNAUTHORIZED);
        }
        log.info("update message request: {}", request.message());
        final var requestDto = UpdateSlackMessageRequest.of(id, request);
        final var responseDto = slackMessageService.updateSlackMessage(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteMessage(
        @PathVariable UUID id, HttpServletRequest servletRequest
    ) {
        Passport passport = passportUtil.getPassportByHttpServletRequest(servletRequest);
        if (passport == null) {
            throw new SlackException(SlackExceptionCode.UNAUTHORIZED);
        }
        slackMessageService.deleteMessage(id);
        return ResponseEntity.ok(CommonResponse.success());
    }

}
