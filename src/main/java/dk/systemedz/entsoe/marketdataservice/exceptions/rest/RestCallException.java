package dk.systemedz.entsoe.marketdataservice.exceptions.rest;

import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.List;

public class RestCallException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 782701139690774063L;

    @Getter @Setter
    private List<ErrorMessageDetailDto> errorMessageDetails;
    @Getter @Setter
    private HttpStatus httpStatus;

    public RestCallException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RestCallException(String message, List<ErrorMessageDetailDto> details, HttpStatus httpStatus) {
        this(message, httpStatus);
        this.errorMessageDetails = details;
    }
}
