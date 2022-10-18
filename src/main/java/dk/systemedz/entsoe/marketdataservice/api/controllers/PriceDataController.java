package dk.systemedz.entsoe.marketdataservice.api.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.systemedz.entsoe.marketdataservice.api.dto.*;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceDataControllerApiDelegate;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.infrastructure.entity.PublicationMarketDocument;
import dk.systemedz.entsoe.marketdataservice.service.EntsoeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.ResponseEntity.ok;

@Component
@Slf4j
public class PriceDataController implements PriceDataControllerApiDelegate {

    private final EntsoeService service;

    public PriceDataController(EntsoeService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<PricesResponseDto> getMarketDocumentFromInterval(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) throws Exception {
        validateInput(areaCode, entsoeSecurityToken, from, to, year, month, week, intervalType, interval);


        Map<String, String> params = new HashMap<>();
        params.put("securityToken", entsoeSecurityToken.trim());
        params.put("documentType", "A44");
        params.put("in_Domain", AreaCode.FR.getAreaCode());
        params.put("out_Domain", AreaCode.FR.getAreaCode());
        params.put("periodStart", from);
        params.put("periodEnd", to);

        try {
            PublicationMarketDocument apiResponse = service.getEntsoeData(params);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String apiResponseJson = objectMapper.writeValueAsString(apiResponse);
            System.out.println(apiResponseJson);
            return ok(PricesResponseDto.builder().build());
        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST, e);
        }
    }

    private void validateInput(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) {
        ErrorMessageDto errorMessage = ErrorMessageDto.builder().message("Invalid parameters provided. See Details for more information.").details(new ArrayList<>()).build();
        if(isNull(areaCode))
            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field("areaCode")
                            .message("Area Code was not valid. Following are available: "+ Arrays.stream(AreaCodeDto.values()).map(Enum::name).collect(Collectors.joining(", ")))
                            .build());

        if(isBlank(entsoeSecurityToken))
            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field("entsoe-security-token")
                            .message("ENTSO-E Security Token has to be provided in the headers.")
                            .build());

        if(isBlank(from) && isNotBlank(to))
            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field("to")
                            .message("You have provided a 'to' date/time, but no 'from' date/time. 'from' is required to search by datetime interval.")
                            .build());

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);

        if(nonNull(year) && year > currentYear)
            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field("year")
                            .message("The year provided has to be before or equals to "+currentYear+".")
                            .build());
        if(nonNull(month)) {
            if(month > 12 || month < 1) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("month")
                                .message("The month provided has to be in the range 1 - 12.")
                                .build());
            }
        }

        if(nonNull(week)) {
            if(week > 53 || week < 1) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("week")
                                .message("The week provided has to be in the range 1 - 53.")
                                .build());
            }
        }

        if(nonNull(year) && nonNull(month)) {
            if(month > currentMonth)
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("month")
                                .message("The month provided has to be before or equals to "+currentMonth+".")
                                .build());
        }

        if(nonNull(year) && nonNull(week)) {
            if(year == currentYear && week > currentWeek)
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("week")
                                .message("The week provided has to be before or equals to "+currentWeek+".")
                                .build());
        }

        if(nonNull(intervalType)) {
            if(isNull(interval)) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("interval")
                                .message("You have provided an intervalType ("+intervalType.getValue().toLowerCase()+"), but no interval.")
                                .build());
            }
            if(intervalType.equals(IntervalTypeDto.YEAR) && (interval > 1 || interval < -1)) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("interval")
                                .message("Yearly Intervals are limited to 1 year at a time. If you want to search for a specific year, please use the ?year= parameter instead.")
                                .build());
            }

            if(intervalType.equals(IntervalTypeDto.MONTH) && (interval > 12 || interval < -12)) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("interval")
                                .message("Monthly Intervals are limited to 12 months at a time (1 year). If you want to search for months further back in time, please use from/to or year/month parameters instead.")
                                .build());
            }
            if(intervalType.equals(IntervalTypeDto.WEEK) && (interval > 53 || interval < -53)) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("interval")
                                .message("Weekly Intervals are limited to 1 year at a time (53 weeks). If you want to search for a week further back in time, please use from/to or year/week parameters instead.")
                                .build());
            }

            if(intervalType.equals(IntervalTypeDto.DAY) && (interval > 365 || interval < -365)) {
                errorMessage.getDetails()
                        .add(ErrorMessageDetailDto.builder()
                                .field("interval")
                                .message("Daily Intervals are limited to 365 days at a time (1 year). If you want to search for days further back in time, please use from/to parameters instead.")
                                .build());
            }
        }

        List<String> fields = new ArrayList<>();
        boolean isSearchingByYearMonthWeek = nonNull(year) || nonNull(month) || nonNull(week);
        boolean isSearchingByInterval = nonNull(intervalType) && nonNull(interval);
        boolean isSearchingByDateTime = isNotBlank(from) && isNotBlank(to);

        if(isSearchingByDateTime && (isSearchingByYearMonthWeek || isSearchingByInterval)) {
            if(nonNull(year)) fields.add("year");
            if(nonNull(month)) fields.add("month");
            if(nonNull(week)) fields.add("week");
            if(nonNull(intervalType)) fields.add("intervalType");
            if(nonNull(interval)) fields.add("interval");

            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field(String.join(", ", fields))
                            .message("You have provided from/to, but also other fields. You can only search by one search method (from/to, year/month/week or intervalType/interval).")
                            .build());
        }


        if(isSearchingByYearMonthWeek && (isSearchingByDateTime || isSearchingByInterval)) {
            fields = new ArrayList<>();
            if(isNotBlank(from)) fields.add("from");
            if(isNotBlank(to)) fields.add("to");
            if(nonNull(intervalType)) fields.add("intervalType");
            if(nonNull(interval)) fields.add("interval");

            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field(String.join(", ", fields))
                            .message("You have provided year/month/week, but also other fields. You can only search by one search method (from/to, year/month/week or intervalType/interval).")
                            .build());
        }

        if(isSearchingByInterval && (isSearchingByDateTime || isSearchingByYearMonthWeek)) {
            fields = new ArrayList<>();
            if(isNotBlank(from)) fields.add("from");
            if(isNotBlank(to)) fields.add("to");
            if(nonNull(year)) fields.add("year");
            if(nonNull(month)) fields.add("month");
            if(nonNull(week)) fields.add("week");

            errorMessage.getDetails()
                    .add(ErrorMessageDetailDto.builder()
                            .field(String.join(", ", fields))
                            .message("You have provided intervalType/interval, but also other fields. You can only search by one search method (from/to, year/month/week or intervalType/interval).")
                            .build());
        }
    }
}
