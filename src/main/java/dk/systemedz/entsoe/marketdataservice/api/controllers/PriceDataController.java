package dk.systemedz.entsoe.marketdataservice.api.controllers;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.mappers.DtoMapper;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceDataControllerApiDelegate;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.service.EntsoeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dk.systemedz.entsoe.marketdataservice.api.validators.RestInputValidatorUtils.*;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceDataController implements PriceDataControllerApiDelegate {

    private final EntsoeService service;
    private final DtoMapper mapper;

    @Override
    public ResponseEntity<PricesResponseDto> getMarketDocumentFromInterval(AreaCodeDto areaCode, String from, String to, Integer year, Integer month, Integer week, String entsoeSecurityToken, IntervalTypeDto intervalType, Integer interval, String securityToken) throws Exception {
        return PriceDataControllerApiDelegate.super.getMarketDocumentFromInterval(areaCode, from, to, year, month, week, entsoeSecurityToken, intervalType, interval, securityToken);
    }

    /*@Override
    public ResponseEntity<PricesResponseDto> getMarketDocumentFromInterval(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) throws Exception {
        assertValidInputs(areaCode, entsoeSecurityToken, from, to, year, month, week, intervalType, interval);

        try {
            boolean isFixed = nonNull(year) || nonNull(month) || nonNull(week);
            boolean isDateTime = isNotBlank(from);
            boolean isInterval = nonNull(intervalType) && nonNull(interval);

            Map<String, String> params = new HashMap<>();
            QueryType queryType;

            if(isFixed) {
                queryType = QueryType.FIXED;
                params.put("year", nonNull(year) ? year.toString() : String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                params.put("month", nonNull(month) ? month.toString() : null);
                params.put("week", nonNull(week) ? week.toString() : null);
            } else if(isDateTime) {
                queryType = QueryType.DATETIME;
                params.put("from", from.trim().substring(0,8));
                params.put("to", isNotBlank(to) ? to.trim().substring(0,8) : LocalDateTime.now()
                        .format(DateTimeFormatter.BASIC_ISO_DATE));
            } else {
                queryType = QueryType.INTERVAL;
                params.put("intervalType", intervalType.name().toUpperCase());
                params.put("interval", String.valueOf(interval));
            }

            MarketDocument marketDocumentResponse = service.
                    getPricesFromEntsoeApi(entsoeSecurityToken.trim(), AreaCode.valueOf(areaCode.name()).getAreaCode(), params, queryType);

            return ok(mapper.mapPricesResponse(marketDocumentResponse));
        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST, e);
        }
    }*/

    private void assertValidInputs(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();
        details.addAll(validateEntsoeSecurityTokenAndAreaCode(entsoeSecurityToken,areaCode));

        boolean isSearchingByYearMonthWeek = nonNull(year) || nonNull(month) || nonNull(week);
        boolean isSearchingByInterval = nonNull(intervalType) || nonNull(interval);
        boolean isSearchingByDateTime = isNotBlank(from) || isNotBlank(to);

        if(isSearchingByYearMonthWeek && !(isSearchingByInterval || isSearchingByDateTime)) {
            details.addAll(validateYearMonthWeek(year, month, week));
        } else if (isSearchingByInterval && !(isSearchingByDateTime || isSearchingByYearMonthWeek)) {
            details.addAll(validateInterval(intervalType, interval));
        } else if(isSearchingByDateTime && !(isSearchingByInterval || isSearchingByYearMonthWeek)) {
            details.addAll(validateDateTimeRange(from, to));
        } else {
            ErrorMessageDetailDto.ErrorMessageDetailDtoBuilder detailBuilder = ErrorMessageDetailDto.builder()
                    .message("You can only use one interval-combination: from/to, year/month/week or intervalType/interval. See more in API documentation.");

            if(isSearchingByDateTime && isSearchingByInterval && isSearchingByYearMonthWeek) {
                detailBuilder.field("from/to, intervalType/interval & year/month/week");
            } else {
                if(isSearchingByDateTime) {
                    String field = "from/to";
                    if(isSearchingByInterval)
                        field += " & intervalType/interval";

                    if(isSearchingByYearMonthWeek)
                        field += " & year/month/week";
                    detailBuilder.field(field);
                } else {
                    detailBuilder.field("intervalType/interval & year/month/week");
                }
            }

            details.add(detailBuilder.build());
        }

        if(!details.isEmpty())
            throw new RestCallException("Invalid parameters provided. See Details for more information.", details, HttpStatus.BAD_REQUEST);
    }
}
