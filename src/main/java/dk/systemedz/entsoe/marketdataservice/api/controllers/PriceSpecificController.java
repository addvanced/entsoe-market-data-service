package dk.systemedz.entsoe.marketdataservice.api.controllers;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.mappers.DtoMapper;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceSpecificControllerApiDelegate;
import dk.systemedz.entsoe.marketdataservice.domain.models.MarketDocument;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.IntervalType;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.QueryType;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.service.EntsoeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateEntsoeSecurityTokenAndAreaCode;
import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateYearMonthWeek;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.ResponseEntity.ok;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceSpecificController implements PriceSpecificControllerApiDelegate {

    private final EntsoeService service;
    private final DtoMapper mapper;


    @Override
    public ResponseEntity<PricesResponseDto> getPricesByMonth(AreaCodeDto areaCode, Integer month, Integer year, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesBySpecific(entsoeSecurityToken, areaCode, IntervalType.MONTH, month, year);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByWeek(AreaCodeDto areaCode, Integer week, Integer year, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesBySpecific(entsoeSecurityToken, areaCode, IntervalType.WEEK, week, year);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByYear(AreaCodeDto areaCode, Integer year, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        MarketDocument prices = getPricesBySpecific(entsoeSecurityToken, areaCode, IntervalType.YEAR, currentYear, currentYear);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesCurrentMonth(AreaCodeDto areaCode, Integer year, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        Integer currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        MarketDocument prices = getPricesBySpecific(entsoeSecurityToken, areaCode, IntervalType.MONTH, currentMonth, year);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesCurrentWeek(AreaCodeDto areaCode, Integer year, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        Integer currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        MarketDocument prices = getPricesBySpecific(entsoeSecurityToken, areaCode, IntervalType.WEEK, currentWeek, year);
        return ok(mapper.mapPricesResponse(prices));
    }

    private MarketDocument getPricesBySpecific(String entsoeSecurityToken, AreaCodeDto areaCode, IntervalType type, Integer specific, Integer year) {
        assertValidSecurityTokenAndAreaCodeInputs(areaCode, entsoeSecurityToken);

        if(isNull(year))
            year = Calendar.getInstance().get(Calendar.YEAR);

        Map<String, String> params = new HashMap<>();
        switch (type) {
            case YEAR -> {
                validateYearMonthWeek(specific,null,null);
                if(nonNull(specific)) {
                    params.put("year", specific.toString());
                }
            }
            case MONTH -> {
                validateYearMonthWeek(year, specific,null);
                if(nonNull(specific)) {
                    params.put("year", year.toString());
                    params.put("month", specific.toString());
                }
            }
            case WEEK -> {
                validateYearMonthWeek(year, null,specific);
                if(nonNull(specific)) {
                    params.put("year", year.toString());
                    params.put("week", specific.toString());
                }
            }
            default -> throw new RestCallException("The specified interval type is not valid.", HttpStatus.BAD_REQUEST);
        }

        try {
            return service.
                    getPricesFromEntsoeApi(entsoeSecurityToken.trim(), AreaCode.valueOf(areaCode.name()).getAreaCode(),
                            params, QueryType.FIXED);
        } catch(RestCallException re) {
            throw new RestCallException("No data provided by ENTSO-E. Try another interval.", HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST, e);
        }
    }


    private void assertValidSecurityTokenAndAreaCodeInputs(AreaCodeDto areaCode, String entsoeSecurityToken) {
        List<ErrorMessageDetailDto> details = new ArrayList<>(validateEntsoeSecurityTokenAndAreaCode(entsoeSecurityToken, areaCode));
        if(!details.isEmpty())
            throw new RestCallException("Invalid parameters provided. See Details for more information.", details, HttpStatus.BAD_REQUEST);
    }
}
