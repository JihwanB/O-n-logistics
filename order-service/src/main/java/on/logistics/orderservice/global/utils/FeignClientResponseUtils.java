package on.logistics.orderservice.global.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import java.io.IOException;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.orderservice.global.presentation.dtos.CommonResponse;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.ExternalApiBadRequestException;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.ExternalApiClientException;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.ExternalApiNotFoundException;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.ExternalApiServerException;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.WrongResponseTypeApiException;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class FeignClientResponseUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static <T> T getBody(Response response, Class<T> responseType) {
        validateResponseStatus(response);
        return parseResponseBody(response, responseType);
    }

    public static List getListBody(Response response, Class<?> responseType) {
        validateResponseStatus(response);
        return parseResponseListBody(response, responseType);
    }

    public static void validateResponseStatus(Response response) {
        int statusCode = response.status();
        if (HttpStatusUtils.isResponseNotFound(statusCode)) {
            throw new ExternalApiNotFoundException();
        }
        if (HttpStatusUtils.isResponseBadRequest(statusCode)) {
            throw new ExternalApiBadRequestException();
        }
        if (HttpStatusUtils.is4xxClientError(statusCode)) {
            throw new ExternalApiClientException();
        }
        if (HttpStatusUtils.is5xxServerError(statusCode)) {
            throw new ExternalApiServerException();
        }
    }

    public static <T> T parseResponseBody(Response response, Class<T> responseType) {
        log.info("응답 바디 파싱");
        try {
            CommonResponse commonResponse = parseCommonResponse(response);
            log.info("응답: {}", commonResponse);
            if (commonResponse == null) {
                return null;
            }
            return parseCommonResponseToT(responseType, commonResponse);
        } catch (IOException e) {
            log.error("잘못된 응답 형식입니다.", e);
            throw new WrongResponseTypeApiException();
        }
    }

    private static CommonResponse parseCommonResponse(Response response) throws IOException {
        return objectMapper.readValue(
            response.body().asInputStream(),
            objectMapper.getTypeFactory().constructType(CommonResponse.class));
    }

    private static <T> T parseCommonResponseToT(Class<T> responseType,
        CommonResponse commonResponse)
        throws JsonProcessingException {
        return objectMapper.readValue(
            objectMapper.writeValueAsString(commonResponse.data()),
            responseType
        );
    }

    public static <T> List parseResponseListBody(Response response, Class<T> responseType) {
        log.info("List 응답 바디 파싱");
        try {
            CommonResponse commonResponse = parseCommonResponse(response);
            log.info("응답: {}", commonResponse);
            if (commonResponse == null) {
                return null;
            }
            List list = parseCommonResponseToList(commonResponse);
            return list.stream().map(o -> {
                try {
                    return parseObjectToT(responseType, o);
                } catch (IOException e) {
                    log.error("잘못된 응답 형식입니다.", e);
                    throw new WrongResponseTypeApiException();
                }
            }).toList();
        } catch (IOException e) {
            log.error("잘못된 응답 형식입니다.", e);
            throw new WrongResponseTypeApiException();
        }
    }

    private static List parseCommonResponseToList(CommonResponse commonResponse)
        throws JsonProcessingException {
        return objectMapper.readValue(
            objectMapper.writeValueAsString(commonResponse.data()),
            List.class
        );
    }

    private static <T> T parseObjectToT(Class<T> responseType, Object o)
        throws JsonProcessingException {
        return objectMapper.readValue(
            objectMapper.writeValueAsString(o),
            responseType
        );
    }
}
