package on.logistics.userservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import on.logistics.userservice.global.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptionCode implements ExceptionCode {
    USER_SLACK_EMAIL_IS_NULL(HttpStatus.BAD_REQUEST, "Slack 이메일은 필수 입력 값입니다."),
    USER_SLACK_EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "Slack 이메일은 이메일 형식으로 입력되어야 합니다."),
    USER_IS_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    USER_IS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "조회 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
