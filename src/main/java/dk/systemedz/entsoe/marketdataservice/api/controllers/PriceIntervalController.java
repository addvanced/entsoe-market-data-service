package dk.systemedz.entsoe.marketdataservice.api.controllers;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.mappers.DtoMapper;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceIntervalControllerApiDelegate;
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

import java.util.*;

import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateEntsoeSecurityTokenAndAreaCode;
import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.validateInterval;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.ResponseEntity.ok;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceIntervalController implements PriceIntervalControllerApiDelegate {

    private final EntsoeService service;
    private final DtoMapper mapper;

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByDayAhead(AreaCodeDto areaCode, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesByInterval(entsoeSecurityToken, areaCode, IntervalTypeDto.DAY, -1);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByYearlyInterval(AreaCodeDto areaCode, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesByInterval(entsoeSecurityToken, areaCode, IntervalTypeDto.YEAR, 1);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByDailyInterval(AreaCodeDto areaCode, Integer interval, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesByInterval(entsoeSecurityToken, areaCode, IntervalTypeDto.DAY, interval);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByMonthlyInterval(AreaCodeDto areaCode, Integer interval, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesByInterval(entsoeSecurityToken, areaCode, IntervalTypeDto.MONTH, interval);
        return ok(mapper.mapPricesResponse(prices));
    }

    @Override
    public ResponseEntity<PricesResponseDto> getPricesByWeeklyInterval(AreaCodeDto areaCode, Integer interval, String entsoeSecurityToken, String securityToken) throws Exception {
        if(isBlank(entsoeSecurityToken))
            entsoeSecurityToken = securityToken;

        MarketDocument prices = getPricesByInterval(entsoeSecurityToken, areaCode, IntervalTypeDto.WEEK, interval);
        return ok(mapper.mapPricesResponse(prices));
    }

    private MarketDocument getPricesByInterval(String entsoeSecurityToken, AreaCodeDto areaCode, IntervalTypeDto intervalType, Integer interval) {
        assertValidIntervalInputs(areaCode, entsoeSecurityToken, intervalType, interval);

        try {
            Map<String, String> params = new HashMap<>();
            params.put("intervalType", intervalType.name().toUpperCase(Locale.ROOT));
            params.put("interval", String.valueOf(interval));

            return service.
                    getPricesFromEntsoeApi(entsoeSecurityToken.trim(), AreaCode.valueOf(areaCode.name()).getAreaCode(), params, QueryType.INTERVAL);
        }  catch (RestCallException re) {
            throw new RestCallException("No data provided by ENTSO-E. Try another interval.", HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST, e);
        }
    }

    private void assertValidIntervalInputs(AreaCodeDto areaCode, String entsoeSecurityToken, IntervalTypeDto intervalType, Integer interval) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();
        details.addAll(validateEntsoeSecurityTokenAndAreaCode(entsoeSecurityToken, areaCode));
        details.addAll(validateInterval(intervalType, interval));

        if(!details.isEmpty())
            throw new RestCallException("Invalid parameters provided. See Details for more information.", details, HttpStatus.BAD_REQUEST);
    }
}
