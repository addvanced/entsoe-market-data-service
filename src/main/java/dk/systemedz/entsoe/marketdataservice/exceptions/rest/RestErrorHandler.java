package dk.systemedz.entsoe.marketdataservice.exceptions.rest;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;

@Slf4j
@RestControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {

    public RestErrorHandler() { super(); }

    @Override
    protected @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<ErrorMessageDetailDto> validationErrors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(f -> ErrorMessageDetailDto.builder()
                        .field(f.getField().trim())
                        .message(f.getDefaultMessage())
                        .build())
                .toList();

        return doHandleExceptionInternal("Bad Request", validationErrors, HttpStatus.BAD_REQUEST, ex, request, headers);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        Pair<String, String> fieldInformation = getRequiredTypeInfo(ex.getRequiredType());

        List<ErrorMessageDetailDto> details = List.of(ErrorMessageDetailDto.builder()
                .field(ex.getName())
                .message("Field should contain a value of type '%s'. Examples: %s".formatted(fieldInformation.getLeft(), fieldInformation.getRight()))
                .build());

        return doHandleExceptionInternal("Invalid Query Parameter: %s=%s".formatted(ex.getName(), ex.getValue()), details, HttpStatus.BAD_REQUEST, ex, request, new HttpHeaders());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        try {
            Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
            String message = String.join(",", constraintViolations.stream().map(ConstraintViolation::getMessage).toList());

            ErrorMessageDto errorResponse = createRestErrorResponse(message, HttpStatus.BAD_REQUEST);
            errorResponse.setDetails(constraintViolations.stream()
                    .map(violation -> ErrorMessageDetailDto.builder()
                            .field(getPropertyName(violation.getPropertyPath()))
                            .message(violation.getMessage())
                            .build())
                    .toList());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            String message = String.join(",", List.of(ex.getMessage()));
            ErrorMessageDto errorResponse = createRestErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();
        return doHandleExceptionInternal("Malformed JSON in Request Body", details, HttpStatus.BAD_REQUEST, ex, request, new HttpHeaders());
    }

    @ExceptionHandler(RestCallException.class)
    protected ResponseEntity<Object> handleRestCallException(RestCallException ex, WebRequest request) {
        return doHandleExceptionInternal(ex.getMessage(), ex.getErrorMessageDetails(), ex.getHttpStatus(), ex, request, new HttpHeaders());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> exceptionHandler(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private String getPropertyName(final Path propertyPath) {
        return StreamSupport.stream(propertyPath.spliterator(), false)
                .filter(node -> !(node.getKind().equals(ElementKind.METHOD)))
                .map(Path.Node::toString)
                .collect(Collectors.joining("."));
    }

    private ResponseEntity<Object> doHandleExceptionInternal(String message, List<ErrorMessageDetailDto> errorDetails, HttpStatus httpStatus, Exception ex, WebRequest request, HttpHeaders headers) {
        ErrorMessageDto errorMessage = createRestErrorResponse(message, errorDetails, httpStatus);

        String errorDetailsString =
                nonNull(errorDetails) && !errorDetails.isEmpty() ?
                        errorDetails.stream()
                                .map(detail -> detail.getField()+"="+detail.getMessage())
                                .collect(Collectors.joining("|")) : "N/A";

        if(HttpStatus.INTERNAL_SERVER_ERROR.equals(httpStatus))
            log.error("Internal Server Error: Message=[{}], ErrorDetails=[{}], Exception={}", message.trim(), errorDetailsString, ex);

        return handleExceptionInternal(ex, errorMessage, headers, httpStatus, request);
    }

    private ErrorMessageDto createRestErrorResponse(String message, HttpStatus httpStatus){
        return createRestErrorResponse(message, null, httpStatus);
    }

    private ErrorMessageDto createRestErrorResponse(String message, List<ErrorMessageDetailDto> errorDetails, HttpStatus httpStatus){
        final String errorMessage = HttpStatus.INTERNAL_SERVER_ERROR.equals(httpStatus) ? "An Internal Server error occurred." : message.trim();
        return ErrorMessageDto.builder()
                .message(errorMessage)
                .details(nonNull(errorDetails) && !errorDetails.isEmpty() ? errorDetails : null)
                .build();
    }

    private Pair<String, String> getRequiredTypeInfo(Class<?> requiredType) {
        if(Integer.class.equals(requiredType)) {
            return Pair.of("Integer", "0, 1, 123, -1, -20 etc.");
        } else if(Boolean.class.equals(requiredType)) {
            return Pair.of("boolean", "true & false");
        } else if(AreaCodeDto.class.equals(requiredType)) {
            return Pair.of("AreaCode", Arrays.stream(AreaCodeDto.values()).map(Enum::name).collect(Collectors.joining(", ")));
        } else if(IntervalTypeDto.class.equals(requiredType)) {
            return Pair.of("IntervalType", Arrays.stream(IntervalTypeDto.values()).map(Enum::name).collect(Collectors.joining(", ")));
        } else {
            return Pair.of("String", "String characters (Alphabets, number, and some special characters)");
        }
    }
}
