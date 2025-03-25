package on.logistics.hubtransitservice.infrastructure.clients.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import on.logistics.hubtransitservice.global.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalApiExceptionCode implements ExceptionCode {

    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "외부 서비스에서 정보를 가져오는 중 오류가 발생했습니다."),
    HUB_BAD_REQUEST(HttpStatus.BAD_REQUEST, "외부 서비스에서 정보를 가져오는 요청이 잘못되었습니다."),
    HUB_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "외부 서비스 호출 도중 클라이언트에서 오류가 발생했습니다."),
    HUB_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서비스에서 오류가 발생했습니다"),
    HUB_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서비스 응답을 파싱하는 도중 오류가 발생했습니다."),
    WRONG_RESPONSE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서비스의 응답 타입이 잘못되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}