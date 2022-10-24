package dk.systemedz.entsoe.marketdataservice.api.validators;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.ErrorMessageDetailDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import dk.systemedz.entsoe.marketdataservice.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RestInputValidatorUtils {

    public static List<ErrorMessageDetailDto> validateEntsoeSecurityTokenAndAreaCode(String entsoeSecurityToken, AreaCodeDto areaCode) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();
        if(isBlank(entsoeSecurityToken)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("entsoe-security-token")
                    .message("ENTSO-E Security Token has to be provided in the header with header key 'entsoe-security-token', " +
                            "or by adding it to the URL with the query parameter '?securityToken='.")
                    .build());
        }
        details.addAll(validateAreaCode(areaCode));
        return details;
    }
    private static List<ErrorMessageDetailDto> validateAreaCode(AreaCodeDto areaCode) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();

        String areaCodeList =
                Arrays.stream(AreaCode.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

        if(isNull(areaCode)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("areaCode")
                    .message("Area Code is required. The following Area Codes are available: " + areaCodeList)
                    .build());
        } else {
            if (!AreaCode.existsByName(areaCode.name())) {
                details.add(ErrorMessageDetailDto.builder()
                        .field("areaCode")
                        .message("Area Code is not valid. The following Area Codes are available: " + areaCodeList)
                        .build());
            }
        }
        return details;
    }
    public static List<ErrorMessageDetailDto> validateDateTimeRange(String from, String to) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();

        if(isBlank(from)){
            details.add(ErrorMessageDetailDto.builder()
                    .field("from")
                    .message("The 'from' parameter is required, when searching by date/time interval with from/to.")
                    .build());
        } else {
            from = from.trim();
            if(!(from.length() == 8 || from.length() == 12)) {
                details.add(ErrorMessageDetailDto.builder()
                        .field("from")
                        .message("Timestamp format has to be either yyyyMMdd or yyyyMMddHHmm. " +
                                "Example: December 1st, 2022 at 23:00 (11PM) would either be 20221201 or 202212012300.")
                        .build());
            } else {
                LocalTime localTime = LocalTime.of(23,0,0,0);
                LocalDate dateTime =
                        LocalDate.parse(from, from.length() == 8 ?
                                DateTimeFormatter.BASIC_ISO_DATE : DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

                LocalDateTime fromDate = DateTimeUtils.createLocalDateTimeFromString(from.substring(0,8));
                LocalDateTime toDate = DateTimeUtils.createLocalDateTimeNow();

                if(fromDate.toLocalDate().isBefore(LocalDate.of(2015,04,01))) {
                    details.add(ErrorMessageDetailDto.builder()
                            .field("from")
                            .message("No data available prior to April 1st, 2015." +
                                    "ENTSO-E does not provide any data prior to April 1st, 2015. Please correct your from-date " +
                                    "to a date equal to, or after April 1st, 2015.")
                            .build());
                }
                if (isNotBlank(to)) {
                    to = to.trim();

                    if (!(to.length() == 8 || to.length() == 12)) {
                        details.add(ErrorMessageDetailDto.builder()
                                .field("to")
                                .message("Timestamp format has to be either yyyyMMdd or yyyyMMddHHmm. " +
                                        "Example: December 1st, 2022 at 23:00 (11PM) would either be 20221201 or 202212012300.")
                                .build());
                    } else {
                        toDate = DateTimeUtils.createLocalDateTimeFromString(to.substring(0,8));

                        if(toDate.toLocalDate().isAfter(LocalDate.now().plusDays(1))) {
                            details.add(ErrorMessageDetailDto.builder()
                                    .field("to")
                                    .message("The to-date can only be set to 1 date after today's date, which is %s" +
                                            "Alternatively, you can look up day-ahead prices by using the endpoint: " +
                                            "/prices/{areaCode}/by-interval/day/next".formatted(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd2300"))))
                                    .build());
                        }
                    }
                }

                if (ChronoUnit.DAYS.between(fromDate, toDate) > 365) {
                    details.add(ErrorMessageDetailDto.builder()
                            .field("from/to")
                            .message("Date range is limited to 365 days (1 year). " +
                                    "If you want to search for days further back in time, " +
                                    "please ensure that there is no more than 365 days between from and to.")
                            .build());
                }
            }
        }
        return details;
    }
    public static List<ErrorMessageDetailDto> validateYearMonthWeek(Integer year, Integer month, Integer week) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);

        if(nonNull(year) && (year > currentYear)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("year")
                    .message("The year provided has to be before or equals to %s.".formatted(currentYear))
                    .build());
        }

        if(nonNull(month) && (month > 12 || month < 1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("month")
                    .message("The month provided has to be in the range 1 - 12.")
                    .build());

        }

        if(nonNull(week) && (week > 53 || week < 1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("week")
                    .message("The week provided has to be in the range 1 - 53.")
                    .build());
        }

        if(nonNull(year) && nonNull(month)) {
            if(year == currentYear && month > currentMonth)
                details.add(ErrorMessageDetailDto.builder()
                        .field("month")
                        .message("The month provided has to be before or equals to %s.".formatted(currentMonth))
                        .build());
        }

        if(nonNull(year) && nonNull(week)) {
            if(year == currentYear && week > currentWeek)
                details.add(ErrorMessageDetailDto.builder()
                        .field("week")
                        .message("The week provided has to be before or equals to "+currentWeek+".")
                        .build());
        }

        if(year < 2015) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("year")
                    .message("No data available prior to April 1st, 2015." +
                            "ENTSO-E does not provide any data prior to April 1st, 2015. Please correct your year" +
                            "to a year equal to, or after 2015.")
                    .build());
        }
        if(year == 2015 && month < 4) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("month")
                    .message("No data available prior to April 1st, 2015." +
                            "ENTSO-E does not provide any data prior to April 1st, 2015. Please correct your month" +
                            "to a month equal to, or after 4 (april).")
                    .build());
        }

        if(year == 2015 && month < 15) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("week")
                    .message("No data available prior to April 1st, 2015." +
                            "ENTSO-E does not provide any data prior to April 1st, 2015. Please correct your week" +
                            "to a week equal to, or after 15 (April 6th - 12th 2015).")
                    .build());
        }

        return details;
    }


    public static List<ErrorMessageDetailDto> validateInterval(IntervalTypeDto intervalType, Integer interval) {
        List<ErrorMessageDetailDto> details = new ArrayList<>();

        if(nonNull(intervalType) && isNull(interval)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("You have provided an intervalType (" + intervalType.getValue().toLowerCase() + "), but no interval.")
                    .build());
        }

        if(interval == 0) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("Interval cannot be set to 0. It should be -1 (only for daily intervals) or greather than 1.")
                    .build());
        }

        if(intervalType.equals(IntervalTypeDto.YEAR) && (interval != 1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("Yearly Intervals are limited to 1 year. " +
                            "If you want to search for a specific year, e.g. 2020, please use the 'year' parameter instead (e.g. /prices/FR?year=2020).")
                    .build());
        }

        if(intervalType.equals(IntervalTypeDto.MONTH) && (interval > 12 || interval < 1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("Monthly Intervals has to be between 1 - 12 months (1 year). " +
                            "If you want to search for months further back in time, please use from/to or year/month parameters instead, " +
                            "e.g. /prices/FR?year=2020&month=10, or /prices/FR?from=20201001&to=20201231")
                    .build());
        }
        if(intervalType.equals(IntervalTypeDto.WEEK) && (interval > 53 || interval < 1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("Weekly Intervals has to be between 1 - 53 weeks (1 year). " +
                            "If you want to search for weeks further back in time, please use from/to or year/week parameters instead, " +
                            "e.g. /prices/FR?year=2020&week=36, or /prices/FR?from=2020831&to=20200906")
                    .build());
        }

        if(intervalType.equals(IntervalTypeDto.DAY) && (interval > 365 || interval < -1)) {
            details.add(ErrorMessageDetailDto.builder()
                    .field("interval")
                    .message("Daily Intervals are limited to -1 - 365 days (day ahead to 1 year). " +
                            "If you want to search for days further back in time, please use from/to parameters instead, " +
                            "e.g. /prices/FR?from=20210801&to=20220731")
                    .build());
        }

        return details;
    }
}
