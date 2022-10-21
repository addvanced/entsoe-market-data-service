package dk.systemedz.entsoe.marketdataservice.api.controllers;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.mappers.DtoMapper;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceRangeControllerApiDelegate;
import dk.systemedz.entsoe.marketdataservice.domain.models.MarketDocument;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.QueryType;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.service.EntsoeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateDateTimeRange;
import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateEntsoeSecurityTokenAndAreaCode;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.ResponseEntity.ok;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceRangeController implements PriceRangeControllerApiDelegate {

    private final EntsoeService service;
    private final DtoMapper mapper;

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByFromDate(AreaCodeDto areaCode, String fromDateTime, String entsoeSecurityToken) throws Exception {
        MarketDocument prices = getPricesByRange(entsoeSecurityToken, areaCode, fromDateTime, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByFromToDate(AreaCodeDto areaCode, String fromDateTime, String entsoeSecurityToken, String toDateTime) throws Exception {
        MarketDocument prices = getPricesByRange(entsoeSecurityToken, areaCode, fromDateTime, toDateTime);
        return ok(mapper.mapPricesResponse(prices));
    }

    private MarketDocument getPricesByRange(String entsoeSecurityToken, AreaCodeDto areaCode, String fromDate, String toDate) {
        assertValidRangeInputs(areaCode, entsoeSecurityToken, fromDate, toDate);

        try {
            Map<String, String> params = new HashMap<>();
            params.put("from", fromDate.trim().substring(0,8));
            params.put("to", isNotBlank(toDate) ? toDate.trim().substring(0,8) : null);

            return service.
                    getPricesFromEntsoeApi(entsoeSecurityToken.trim(), AreaCode.valueOf(areaCode.name()).getAreaCode(),
                            params, QueryType.DATETIME);
        }  catch (RuntimeException e) {
            log.error("Error:", e);
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST, e);
        }
    }

    private void assertValidRangeInputs(AreaCodeDto areaCode, String entsoeSecurityToken, String fromDateTime, String toDateTime) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();
        details.addAll(validateEntsoeSecurityTokenAndAreaCode(entsoeSecurityToken, areaCode));
        details.addAll(validateDateTimeRange(fromDateTime, toDateTime));

        if(!details.isEmpty())
            throw new RestCallException("Invalid parameters provided. See Details for more information.", details, HttpStatus.BAD_REQUEST);
    }
}
